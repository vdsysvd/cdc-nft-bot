package it.nftspace.cdcnftbot.client.request;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PostDataRequest {

    private String operationName;
    private Variables variables;
    private String query;

    @Data
    @Builder
    public static class Variables {

        private String collectionId;
        private String cacheId;
        private int first;
        private int skip;
        private List<SortRequest> sort;
        private WhereRequest where;

    }

}
