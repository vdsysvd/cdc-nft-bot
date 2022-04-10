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
            "BALLIES", "6c7b1a68479f2fc35e9f81e42bcb7397");

    @Scheduled(fixedDelay = 10000)
    public void run(){
        restClientService.auction(COLLECTIONS.get("ALPHABOT"), 420, 1, 10);
        restClientService.auction(COLLECTIONS.get("BALLIES"), 250, 1, 10);
    }

    @Scheduled(fixedDelay = 10000)
    public void buyNow(){
        restClientService.buyNow(COLLECTIONS.get("ALPHABOT"), 420);
        restClientService.buyNow(COLLECTIONS.get("BALLIES"), 250);
    }

    @Scheduled(fixedDelay = 60*60 * 1000)
    public void cache(){
        CACHE.clear();
    }

}
