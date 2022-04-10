package it.nftspace.cdcnftbot.client;

import it.nftspace.cdcnftbot.App;
import it.nftspace.cdcnftbot.client.request.PostDataRequest;
import it.nftspace.cdcnftbot.client.request.SortRequest;
import it.nftspace.cdcnftbot.client.request.WhereRequest;
import it.nftspace.cdcnftbot.client.response.AssetResponse;
import it.nftspace.cdcnftbot.client.response.DataResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.stream.Stream;

@Service @RequiredArgsConstructor public class RestClientService {

    @Value("${telegram.token:}") private String telegramToken;
    @Value("${telegram.chat_id:}") private String telegramChatId;

    private static final String TELEGRAM_SEND_MESSAGE = "https://api.telegram.org/bot%s/sendMessage?chat_id=%s&text=%s";

    private final RestTemplate restTemplate;
    private static final String BASE_URL = "https://crypto.com/nft-api/graphql";
    public static final String GET_COLLECTION_QUERY =
        "query GetCollection($collectionId: ID!) {\n  public {\n    collection(id: $collectionId) {\n      id\n      name\n      description\n      categories\n      banner {\n        url\n        __typename\n      }\n      logo {\n        url\n        __typename\n      }\n      creator {\n        displayName\n        __typename\n      }\n      aggregatedAttributes {\n        label: traitType\n        options: attributes {\n          value: id\n          label: value\n          total\n          __typename\n        }\n        __typename\n      }\n      metrics {\n        items\n        minAuctionListingPriceDecimal\n        minSaleListingPriceDecimal\n        owners\n        totalSalesDecimal\n        __typename\n      }\n      __typename\n    }\n    __typename\n  }\n}\n";

    public static final String GET_ASSET_QUERY = "fragment UserData on User {\n  uuid\n  id\n  username\n  displayName\n  isCreator\n  avatar {\n    url\n    __typename\n  }\n  isCreationWithdrawalBlocked\n  creationWithdrawalBlockExpiredAt\n  verified\n  __typename\n}\n\nquery GetAssets($audience: Audience, $brandId: ID, $categories: [ID!], $collectionId: ID, $creatorId: ID, $ownerId: ID, $first: Int!, $skip: Int!, $cacheId: ID, $hasSecondaryListing: Boolean, $where: AssetsSearch, $sort: [SingleFieldSort!], $isCurated: Boolean, $createdPublicView: Boolean) {\n  public(cacheId: $cacheId) {\n    assets(\n      audience: $audience\n      brandId: $brandId\n      categories: $categories\n      collectionId: $collectionId\n      creatorId: $creatorId\n      ownerId: $ownerId\n      first: $first\n      skip: $skip\n      hasSecondaryListing: $hasSecondaryListing\n      where: $where\n      sort: $sort\n      isCurated: $isCurated\n      createdPublicView: $createdPublicView\n    ) {\n      id\n      name\n      copies\n      copiesInCirculation\n      creator {\n        ...UserData\n        __typename\n      }\n      main {\n        url\n        __typename\n      }\n      cover {\n        url\n        __typename\n      }\n      royaltiesRateDecimal\n      primaryListingsCount\n      secondaryListingsCount\n      primarySalesCount\n      totalSalesDecimal\n      defaultListing {\n        editionId\n        priceDecimal\n        mode\n        auctionHasBids\n        __typename\n      }\n      defaultAuctionListing {\n        editionId\n        priceDecimal\n        auctionMinPriceDecimal\n        auctionCloseAt\n        mode\n        auctionHasBids\n        __typename\n      }\n      defaultSaleListing {\n        editionId\n        priceDecimal\n        mode\n        __typename\n      }\n      defaultPrimaryListing {\n        editionId\n        priceDecimal\n        mode\n        auctionHasBids\n        primary\n        __typename\n      }\n      defaultSecondaryListing {\n        editionId\n        priceDecimal\n        mode\n        auctionHasBids\n        __typename\n      }\n      defaultSecondaryAuctionListing {\n        editionId\n        priceDecimal\n        auctionMinPriceDecimal\n        auctionCloseAt\n        mode\n        auctionHasBids\n        __typename\n      }\n      defaultSecondarySaleListing {\n        editionId\n        priceDecimal\n        mode\n        __typename\n      }\n      likes\n      views\n      isCurated\n      defaultEditionId\n      isLiked\n      isAcceptingOffers\n      externalNftMetadata {\n        creatorAddress\n        creator {\n          name\n          avatar {\n            url\n            __typename\n          }\n          __typename\n        }\n        network\n        __typename\n      }\n      __typename\n    }\n    __typename\n  }\n}\n";

    private BigDecimal getFloorPrice(String collectionId) {
        PostDataRequest postDataRequest = PostDataRequest.builder().operationName("GetCollection")
            .variables(PostDataRequest.Variables.builder().collectionId(collectionId)
                .build()).query(GET_COLLECTION_QUERY).build();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<PostDataRequest> request = new HttpEntity<>(postDataRequest, headers);
        var res = restTemplate.postForEntity(BASE_URL, request, DataResponse.class);
        return res.getBody().getData().getPublicData().getCollection().getMetrics()
            .getMinSaleListingPriceDecimal();
    }

    public void auction(String collectionId, int hours, int spread) {
        var floorPrice = getFloorPrice(collectionId);
        PostDataRequest postDataRequest = PostDataRequest.builder().operationName("GetAssets")
            .variables(PostDataRequest.Variables.builder().collectionId(collectionId)
                .first(100).sort(Collections.singletonList(
                    SortRequest.builder().field("auctionCloseAt").order("ASC").build())).build())
            .query(GET_ASSET_QUERY).build();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<PostDataRequest> request = new HttpEntity<>(postDataRequest, headers);
        var res = restTemplate.postForEntity(BASE_URL, request, DataResponse.class);
        StringBuilder print = new StringBuilder();
        res.getBody().getData().getPublicData().getAssets().stream()
            .filter(f -> opportunitiesFilter(f, floorPrice, spread, hours)).forEach(a -> {
                var info = info(a, collectionId);
                if (!AppScheduler.CACHE.contains(info)) {
                    AppScheduler.CACHE.add(info);
                    print.append(floorPrice).append(" ")
                        .append(info).append("\n");
                }
            });
        String out = print.toString();
        if(!out.equals("")) {
            System.out.println(collectionId + " auction " + "Floor-Price " + floorPrice.intValue() + " Auction SPREAD selected " + spread);
            Stream.of(out.split("\n")).forEach(i -> sendMessage(i.replace("#", "id-")));
            System.out.print(out);
        }
    }

    public void buyNow(String collectionId, long maxPrice) {
        var floorPrice = getFloorPrice(collectionId);
        PostDataRequest postDataRequest = PostDataRequest.builder().operationName("GetAssets")
            .variables(PostDataRequest.Variables.builder().collectionId(collectionId)
                .first(100).where(WhereRequest.builder().buyNow(true)
                    .maxPrice(BigDecimal.valueOf(maxPrice).toString()).build()).sort(
                    Collections.singletonList(
                        SortRequest.builder().field("listingTime").order("DESC").build())).build())
            .query(GET_ASSET_QUERY).build();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<PostDataRequest> request = new HttpEntity<>(postDataRequest, headers);
        var res = restTemplate.postForEntity(BASE_URL, request, DataResponse.class);
        var assets =res.getBody().getData().getPublicData().getAssets();
        if(assets != null){
            StringBuilder print = new StringBuilder();
            assets.forEach(a -> {
                var map = App.COLLECTIONS.get(collectionId);
                String ranking = map != null ? map.get(a.getName().split(" ")[1]): "NotAvail";
                var info = "buyNow " + a.getName() + " "  + ranking + " " + a.getDefaultListing().getPriceDecimal();
                if (!AppScheduler.CACHE.contains(info)) {
                    AppScheduler.CACHE.add(info);
                    print.append(info).append("\n");
                }
            });
            String out = print.toString();
            //Stream.of(out.split("\n")).map(i -> i.split(" ")[3]).sorted(Comparator.comparingInt(Integer::parseInt)).collect(Collectors.toList());
            if(!out.equals("")) {
                System.out.println(collectionId + " buyNow floor-price" + floorPrice + " maxPrice " + maxPrice);
                System.out.print(out);
                Stream.of(out.split("\n")).forEach(i -> sendMessage(i.replace("#", "id-")));
            }
        }
    }

    private boolean opportunitiesFilter(AssetResponse assetResponse, BigDecimal floorPrice, long spread, int hours) {
        var listing = assetResponse.getDefaultAuctionListing();
        var price = listing != null ? listing.getPriceDecimal() : null;
        var auctionClose = listing != null ? listing.getAuctionCloseAt() : null;
        return price != null && floorPrice.subtract(price).intValue() > spread
            && auctionClose != null && auctionClose.isBefore(ZonedDateTime.now().plusHours(hours));
    }

    private String info(AssetResponse assetResponse, String collectionId) {
        String name = assetResponse.getName();
        var listing = assetResponse.getDefaultAuctionListing();
        String bid = listing.getPriceDecimal() != null ? listing.getPriceDecimal().toString() : "-";
        String date =
            listing.getAuctionCloseAt() != null ? listing.getAuctionCloseAt().toString() : "-";
        String ranking = App.COLLECTIONS.get(collectionId).get(name.split(" ")[1]);
        return name + " " + ranking + " " + bid + " " + date;
    }

    public void sendMessage(String text) {
        restTemplate.getForEntity(String.format(TELEGRAM_SEND_MESSAGE, telegramToken, telegramChatId, text), String.class);
    }
}
