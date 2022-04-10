package it.nftspace.cdcnftbot.client;

import it.nftspace.cdcnftbot.App;
import it.nftspace.cdcnftbot.client.request.PostDataRequest;
import it.nftspace.cdcnftbot.client.request.SortRequest;
import it.nftspace.cdcnftbot.client.response.AssetResponse;
import it.nftspace.cdcnftbot.client.response.DataResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Service @RequiredArgsConstructor public class RestClientService {

    private static final Map<String, String> COLLECTIONS =
        Map.of("ALPHABOT", "4ff90f089ac3ef9ce342186adc48a30d");
    private static final String QUERY =
        "fragment UserData on User {\n  uuid\n  id\n  username\n  displayName\n  isCreator\n  avatar {\n    url\n    __typename\n  }\n  isCreationWithdrawalBlocked\n  creationWithdrawalBlockExpiredAt\n  verified\n  __typename\n}\n\nquery GetAssets($audience: Audience, $brandId: ID, $categories: [ID!], $collectionId: ID, $creatorId: ID, $ownerId: ID, $first: Int!, $skip: Int!, $cacheId: ID, $hasSecondaryListing: Boolean, $where: AssetsSearch, $sort: [SingleFieldSort!], $isCurated: Boolean, $createdPublicView: Boolean) {\n  public(cacheId: $cacheId) {\n    assets(\n      audience: $audience\n      brandId: $brandId\n      categories: $categories\n      collectionId: $collectionId\n      creatorId: $creatorId\n      ownerId: $ownerId\n      first: $first\n      skip: $skip\n      hasSecondaryListing: $hasSecondaryListing\n      where: $where\n      sort: $sort\n      isCurated: $isCurated\n      createdPublicView: $createdPublicView\n    ) {\n      id\n      name\n      copies\n      copiesInCirculation\n      creator {\n        ...UserData\n        __typename\n      }\n      main {\n        url\n        __typename\n      }\n      cover {\n        url\n        __typename\n      }\n      royaltiesRateDecimal\n      primaryListingsCount\n      secondaryListingsCount\n      primarySalesCount\n      totalSalesDecimal\n      defaultListing {\n        editionId\n        priceDecimal\n        mode\n        auctionHasBids\n        __typename\n      }\n      defaultAuctionListing {\n        editionId\n        priceDecimal\n        auctionMinPriceDecimal\n        auctionCloseAt\n        mode\n        auctionHasBids\n        __typename\n      }\n      defaultSaleListing {\n        editionId\n        priceDecimal\n        mode\n        __typename\n      }\n      defaultPrimaryListing {\n        editionId\n        priceDecimal\n        mode\n        auctionHasBids\n        primary\n        __typename\n      }\n      defaultSecondaryListing {\n        editionId\n        priceDecimal\n        mode\n        auctionHasBids\n        __typename\n      }\n      defaultSecondaryAuctionListing {\n        editionId\n        priceDecimal\n        auctionMinPriceDecimal\n        auctionCloseAt\n        mode\n        auctionHasBids\n        __typename\n      }\n      defaultSecondarySaleListing {\n        editionId\n        priceDecimal\n        mode\n        __typename\n      }\n      likes\n      views\n      isCurated\n      defaultEditionId\n      isLiked\n      isAcceptingOffers\n      externalNftMetadata {\n        creatorAddress\n        creator {\n          name\n          avatar {\n            url\n            __typename\n          }\n          __typename\n        }\n        network\n        __typename\n      }\n      __typename\n    }\n    __typename\n  }\n}\n";
    private static final int SPREAD = 1;
    private static final int HOURS = 2;
    private final RestTemplate restTemplate;
    private static final String BASE_URL = "https://crypto.com/nft-api/graphql";
    public static final String GET_COLLECTION_QUERY =
        "query GetCollection($collectionId: ID!) {\n  public {\n    collection(id: $collectionId) {\n      id\n      name\n      description\n      categories\n      banner {\n        url\n        __typename\n      }\n      logo {\n        url\n        __typename\n      }\n      creator {\n        displayName\n        __typename\n      }\n      aggregatedAttributes {\n        label: traitType\n        options: attributes {\n          value: id\n          label: value\n          total\n          __typename\n        }\n        __typename\n      }\n      metrics {\n        items\n        minAuctionListingPriceDecimal\n        minSaleListingPriceDecimal\n        owners\n        totalSalesDecimal\n        __typename\n      }\n      __typename\n    }\n    __typename\n  }\n}\n";

    private BigDecimal getFloorPrice() {
        PostDataRequest postDataRequest = PostDataRequest.builder().operationName("GetCollection")
            .variables(PostDataRequest.Variables.builder().collectionId(COLLECTIONS.get("ALPHABOT"))
                .build()).query(GET_COLLECTION_QUERY).build();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<PostDataRequest> request = new HttpEntity<>(postDataRequest, headers);
        var res = restTemplate.postForEntity(BASE_URL, request, DataResponse.class);
        return res.getBody().getData().getPublicData().getCollection().getMetrics()
            .getMinSaleListingPriceDecimal();
    }

    public void call() {
        var floorPrice = getFloorPrice();
        //System.out.println("ALPHABOT " + "Floor-Price " + floorPrice.intValue() + " SPREAD selected " + SPREAD);
        PostDataRequest postDataRequest = PostDataRequest.builder().operationName("GetAssets")
            .variables(PostDataRequest.Variables.builder().collectionId(COLLECTIONS.get("ALPHABOT"))
                .first(100).sort(Collections.singletonList(
                    SortRequest.builder().field("auctionCloseAt").order("ASC").build())).build())
            .query(QUERY).build();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<PostDataRequest> request = new HttpEntity<>(postDataRequest, headers);
        var res = restTemplate.postForEntity(BASE_URL, request, DataResponse.class);
        res.getBody().getData().getPublicData().getAssets().stream()
            .filter(f -> opportunitiesFilter(f, floorPrice)).forEach(a -> {
                var info = info(a);
                if (!AppScheduler.CACHE.contains(info)) {
                    AppScheduler.CACHE.add(info);
                    System.out.println("Floor-price" + floorPrice + "! " + info);
                }
            });
    }

    private boolean opportunitiesFilter(AssetResponse assetResponse, BigDecimal floorPrice) {
        var listing = assetResponse.getDefaultAuctionListing();
        var price = listing != null ? listing.getPriceDecimal() : null;
        var auctionClose = listing != null ? listing.getAuctionCloseAt() : null;
        return price != null && floorPrice.subtract(price).intValue() > SPREAD
            && auctionClose != null && auctionClose.isBefore(ZonedDateTime.now().plusHours(HOURS));
    }

    private String info(AssetResponse assetResponse) {
        String name = assetResponse.getName();
        var listing = assetResponse.getDefaultAuctionListing();
        String bid = listing.getPriceDecimal() != null ? listing.getPriceDecimal().toString() : "-";
        String date =
            listing.getAuctionCloseAt() != null ? listing.getAuctionCloseAt().toString() : "-";
        String ranking = App.ALPHA_BOT.get(name.split(" ")[1]);
        return name + " " + ranking + " " + bid + " " + date;
    }

}
