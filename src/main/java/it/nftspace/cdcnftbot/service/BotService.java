package it.nftspace.cdcnftbot.service;

import it.nftspace.cdcnftbot.client.RestClientService;
import it.nftspace.cdcnftbot.client.request.SortRequest;
import it.nftspace.cdcnftbot.client.response.AssetResponse;
import it.nftspace.cdcnftbot.client.response.DataResponse;
import it.nftspace.cdcnftbot.config.AppSchedulerConfig;
import it.nftspace.cdcnftbot.config.CdcCollection;
import it.nftspace.cdcnftbot.config.CdcInitCollections;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;

@Service @RequiredArgsConstructor public class BotService {

    private final RestClientService restClientService;

    public void buyNow(String collectionId, long maxPrice, int limit) {
        var floorPrice = restClientService.buyNowGetFloorPrice(collectionId);
        var assets = restClientService.getAssets(collectionId, maxPrice, limit, true,
                Collections.singletonList(
                    SortRequest.builder().field("listingTime").order("DESC").build())).getData()
            .getPublicData().getAssets();
        if (assets != null) {
            StringBuilder print = new StringBuilder();
            assets.forEach(a -> {
                var map = CdcInitCollections.COLLECTIONS.get(collectionId);
                String ranking = map != null ? map.get(getAssetId(a.getName())) : "NotAvail";
                var info = "buyNow " + a.getName() + " " + ranking + " " + a.getDefaultListing()
                    .getPriceDecimal();
                if (!AppSchedulerConfig.CACHE.contains(info)) {
                    AppSchedulerConfig.CACHE.add(info);
                    print.append(info).append("\n");
                }
            });
            String out = print.toString();
            //Stream.of(out.split("\n")).map(i -> i.split(" ")[3]).sorted(Comparator.comparingInt(Integer::parseInt)).collect(Collectors.toList());
            if (!out.equals("")) {
                System.out.println(
                    collectionId + " buyNow floor-price" + floorPrice + " maxPrice " + maxPrice);
                System.out.print(out);
                Stream.of(out.split("\n"))
                    .forEach(i -> restClientService.sendMessage(i.replace("#", "id-")));
            }
        }
    }

    public void auction(String collectionId, int hours, int spread, int limit) {
        var floorPrice = restClientService.buyNowGetFloorPrice(collectionId);
        StringBuilder print = new StringBuilder();
        var assets = restClientService.getAssets(collectionId, 750, limit, false,
                Collections.singletonList(
                    SortRequest.builder().field("auctionCloseAt").order("ASC").build())).getData()
            .getPublicData().getAssets();
        assets.stream()
            .filter(f -> opportunitiesFilter(f, floorPrice, spread, hours)).forEach(a -> {
                var info = info(a, collectionId);
                if (!AppSchedulerConfig.CACHE.contains(info)) {
                    AppSchedulerConfig.CACHE.add(info);
                    print.append(floorPrice).append(" ")
                        .append(info).append("\n");
                }
            });
        String out = print.toString();
        if(!out.equals("")) {
            System.out.println(collectionId + " auction " + "Floor-Price " + floorPrice.intValue() + " Auction SPREAD selected " + spread);
            Stream.of(out.split("\n")).forEach(i -> restClientService.sendMessage(i.replace("#", "id-")));
            System.out.print(out);
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
        var map = CdcInitCollections.COLLECTIONS.get(collectionId);
        String ranking = map != null ? map.get(getAssetId(name)): "NotAvail";
        return name + " " + ranking + " " + bid + " " + date;
    }

    private String getAssetId(String name){
        if(name.contains("Loaded")){
            return name.split(" ")[2];
        }
        return name.split(" ")[1];
    }

    @Async
    public void topListed(CdcCollection cdcCollection, int maxPrice, int limit, boolean sortByPrice) {

        Map<String, Object[]> rankNumber = new TreeMap<>();
        DataResponse res =
            restClientService.getAssets(cdcCollection.toString(), maxPrice, limit, true, Collections.emptyList());
        var assets = res.getData().getPublicData().getAssets();
        if (assets != null) {
            assets.forEach(a -> {
                var map = CdcInitCollections.COLLECTIONS.get(cdcCollection.toString());
                String assetId = getAssetId(a.getName());
                String ranking = map != null ? map.get(assetId) : null;
                var price = a.getDefaultListing().getPriceDecimal();
                if (ranking != null) {
                    rankNumber.put(ranking, new Object[]{assetId.replace("#", "id-"), price});
                }
            });
        }
        restClientService.sendMessage("TopListed for " + cdcCollection.name() + " maxPrice " + maxPrice + " sortByPrice" + sortByPrice);
        if(sortByPrice){
            rankNumber.entrySet().stream().sorted(
                    Comparator.comparing(o -> ((BigDecimal) o.getValue()[1])))
                .forEach(e -> restClientService.sendMessage("rank " + e.getKey() + " " + e.getValue()[0] + " $" + e.getValue()[1]));
        } else {
            rankNumber.forEach( (k, v) -> restClientService.sendMessage("rank " + k + " " + v[0] + " $" + v[1]));
        }

    }

}
