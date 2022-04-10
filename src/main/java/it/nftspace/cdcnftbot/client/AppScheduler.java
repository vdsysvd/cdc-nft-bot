package it.nftspace.cdcnftbot.client;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class AppScheduler {

    private final RestClientService restClientService;
    public static final Set<String> CACHE = new HashSet<>();
    private static final Map<String, String> COLLECTIONS =
        Map.of("ALPHABOT", "4ff90f089ac3ef9ce342186adc48a30d",
            "BALLIES", "6c7b1a68479f2fc35e9f81e42bcb7397",
            "LL", "82421cf8e15df0edcaa200af752a344f",
            "PSYCHOKITTIES", "faa3d8da88f9ee2f25267e895db71471");

    @Scheduled(fixedDelay = 10000)
    public void alphabot(){
        restClientService.auction(COLLECTIONS.get("ALPHABOT"), 1, 50);
        restClientService.buyNow(COLLECTIONS.get("ALPHABOT"), 420);
    }

    @Scheduled(fixedDelay = 10000)
    public void ballies(){
        restClientService.auction(COLLECTIONS.get("BALLIES"), 1, 50);
        restClientService.buyNow(COLLECTIONS.get("BALLIES"), 250);
    }

    @Scheduled(fixedDelay = 10000)
    public void ll(){
        restClientService.auction(COLLECTIONS.get("LL"), 1, 300);
        restClientService.buyNow(COLLECTIONS.get("LL"), 3900);
    }

    @Scheduled(fixedDelay = 10000)
    public void psychok(){
        restClientService.auction(COLLECTIONS.get("PSYCHOKITTIES"), 1, 70);
        restClientService.buyNow(COLLECTIONS.get("PSYCHOKITTIES"), 550);
    }


    @Scheduled(fixedDelay = 60*60 * 1000)
    public void cache(){
        CACHE.clear();
    }

}
