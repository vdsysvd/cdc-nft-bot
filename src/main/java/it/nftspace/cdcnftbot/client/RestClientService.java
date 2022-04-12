package it.nftspace.cdcnftbot.client;

import it.nftspace.cdcnftbot.client.request.PostDataRequest;
import it.nftspace.cdcnftbot.client.request.SortRequest;
import it.nftspace.cdcnftbot.client.request.WhereRequest;
import it.nftspace.cdcnftbot.client.response.DataResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service @RequiredArgsConstructor public class RestClientService {

    private static final String TELEGRAM_SEND_MESSAGE = "https://api.telegram.org/bot%s/sendMessage?chat_id=%s&text=%s";
    private static final String GET_ASSETS = "GetAssets";
    private static final String BASE_URL = "https://crypto.com/nft-api/graphql";
    private static final String GET_COLLECTION_QUERY = "query GetCollection($collectionId: ID!) {\n  public {\n    collection(id: $collectionId) {\n      id\n      name\n      description\n      categories\n      banner {\n        url\n        __typename\n      }\n      logo {\n        url\n        __typename\n      }\n      creator {\n        displayName\n        __typename\n      }\n      aggregatedAttributes {\n        label: traitType\n        options: attributes {\n          value: id\n          label: value\n          total\n          __typename\n        }\n        __typename\n      }\n      metrics {\n        items\n        minAuctionListingPriceDecimal\n        minSaleListingPriceDecimal\n        owners\n        totalSalesDecimal\n        __typename\n      }\n      __typename\n    }\n    __typename\n  }\n}\n";
    public static final String GET_ASSETS_QUERY = "fragment UserData on User {\n  uuid\n  id\n  username\n  displayName\n  isCreator\n  avatar {\n    url\n    __typename\n  }\n  isCreationWithdrawalBlocked\n  creationWithdrawalBlockExpiredAt\n  verified\n  __typename\n}\n\nquery GetAssets($audience: Audience, $brandId: ID, $categories: [ID!], $collectionId: ID, $creatorId: ID, $ownerId: ID, $first: Int!, $skip: Int!, $cacheId: ID, $hasSecondaryListing: Boolean, $where: AssetsSearch, $sort: [SingleFieldSort!], $isCurated: Boolean, $createdPublicView: Boolean) {\n  public(cacheId: $cacheId) {\n    assets(\n      audience: $audience\n      brandId: $brandId\n      categories: $categories\n      collectionId: $collectionId\n      creatorId: $creatorId\n      ownerId: $ownerId\n      first: $first\n      skip: $skip\n      hasSecondaryListing: $hasSecondaryListing\n      where: $where\n      sort: $sort\n      isCurated: $isCurated\n      createdPublicView: $createdPublicView\n    ) {\n      id\n      name\n      copies\n      copiesInCirculation\n      creator {\n        ...UserData\n        __typename\n      }\n      main {\n        url\n        __typename\n      }\n      cover {\n        url\n        __typename\n      }\n      royaltiesRateDecimal\n      primaryListingsCount\n      secondaryListingsCount\n      primarySalesCount\n      totalSalesDecimal\n      defaultListing {\n        editionId\n        priceDecimal\n        mode\n        auctionHasBids\n        __typename\n      }\n      defaultAuctionListing {\n        editionId\n        priceDecimal\n        auctionMinPriceDecimal\n        auctionCloseAt\n        mode\n        auctionHasBids\n        __typename\n      }\n      defaultSaleListing {\n        editionId\n        priceDecimal\n        mode\n        __typename\n      }\n      defaultPrimaryListing {\n        editionId\n        priceDecimal\n        mode\n        auctionHasBids\n        primary\n        __typename\n      }\n      defaultSecondaryListing {\n        editionId\n        priceDecimal\n        mode\n        auctionHasBids\n        __typename\n      }\n      defaultSecondaryAuctionListing {\n        editionId\n        priceDecimal\n        auctionMinPriceDecimal\n        auctionCloseAt\n        mode\n        auctionHasBids\n        __typename\n      }\n      defaultSecondarySaleListing {\n        editionId\n        priceDecimal\n        mode\n        __typename\n      }\n      likes\n      views\n      isCurated\n      defaultEditionId\n      isLiked\n      isAcceptingOffers\n      externalNftMetadata {\n        creatorAddress\n        creator {\n          name\n          avatar {\n            url\n            __typename\n          }\n          __typename\n        }\n        network\n        __typename\n      }\n      __typename\n    }\n    __typename\n  }\n}\n";

    @Value("${telegram.token:}") private String telegramToken;
    @Value("${telegram.chat_id:}") private String telegramChatId;
    private final RestTemplate restTemplate;


    public BigDecimal buyNowGetFloorPrice(String collectionId) {
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

    public DataResponse getAssets(String collectionId, long maxPrice, int limit, boolean buyNow, List<SortRequest> sortRequests) {
        PostDataRequest postDataRequest = PostDataRequest.builder().operationName(GET_ASSETS)
            .variables(PostDataRequest.Variables.builder().collectionId(collectionId).first(limit).where(WhereRequest.builder().buyNow(buyNow)
                    .maxPrice(BigDecimal.valueOf(maxPrice).toString()).build()).sort(
                    sortRequests).build())
            .query(GET_ASSETS_QUERY).build();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<PostDataRequest> request = new HttpEntity<>(postDataRequest, headers);
        var res = restTemplate.postForEntity(BASE_URL, request, DataResponse.class);
        return res.getBody();
    }

    public void sendMessage(String text) {
        log.info(text);
        restTemplate.getForEntity(String.format(TELEGRAM_SEND_MESSAGE, telegramToken, telegramChatId, text), String.class);
    }
}
