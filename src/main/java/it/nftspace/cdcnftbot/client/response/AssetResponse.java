package it.nftspace.cdcnftbot.client.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Data
public class AssetResponse {

    private String id;
    private String name;
    private Listing defaultAuctionListing;
    private Listing defaultListing;

    @Data
    public static class Listing {
        private String editionId;
        private BigDecimal priceDecimal;
        private BigDecimal auctionMinPriceDecimal;
        private String mode;
        private ZonedDateTime auctionCloseAt;
        private boolean auctionHasBids;
    }



}
