package it.nftspace.cdcnftbot.config;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public enum CdcCollection {

    LL("82421cf8e15df0edcaa200af752a344f"),
    BALLIES("6c7b1a68479f2fc35e9f81e42bcb7397"),
    ALPHA_BOTS("4ff90f089ac3ef9ce342186adc48a30d"),
    PSYCHO_KITTIES("faa3d8da88f9ee2f25267e895db71471");

    private static final Map<String, String> LOOK_UP = new HashMap<>();
    static {
        for (CdcCollection orderStatus : EnumSet.allOf(CdcCollection.class)) {
            LOOK_UP.put(orderStatus.toString(), orderStatus.name());
        }
    }

    String collectionId;

    CdcCollection(String collectionId) {
        this.collectionId = collectionId;
    }

    public static String getCollectionName(String collectionId){
        return LOOK_UP.get(collectionId);
    }

    @Override public String toString() {
        return collectionId;
    }
}
