package it.nftspace.cdcnftbot.client.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data public class DataResponse {

    private InnerData data;


    @Data public static class InnerData {

        @JsonProperty("public") private PublicData publicData;


        @Data public static class PublicData {
            private List<AssetResponse> assets;
            private Collection collection;
        }


        @Data public static class Collection {
            private String id;
            private String name;
            private Metrics metrics;


            @Data public static class Metrics {
                private BigDecimal minAuctionListingPriceDecimal;
                private BigDecimal minSaleListingPriceDecimal;
            }
        }

    }



}
