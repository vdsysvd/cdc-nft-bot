package it.nftspace.cdcnftbot.config;

public enum CdcCollection {

    LL("82421cf8e15df0edcaa200af752a344f"),
    BALLIES("6c7b1a68479f2fc35e9f81e42bcb7397"),
    ALPHA_BOT("4ff90f089ac3ef9ce342186adc48a30d"),
    PSYCHO_KITTIES("faa3d8da88f9ee2f25267e895db71471");

    String collectionId;

    CdcCollection(String collectionId) {
        this.collectionId = collectionId;
    }

    @Override public String toString() {
        return collectionId;
    }
}
