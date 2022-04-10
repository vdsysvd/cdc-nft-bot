package it.nftspace.cdcnftbot.client.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SortRequest {

    private String order;
    private String field;

}
