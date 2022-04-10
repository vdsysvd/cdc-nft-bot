package it.nftspace.cdcnftbot.client.request;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class WhereRequest {

    private boolean auction;
    private boolean buyNow;
    private String maxPrice;

}
