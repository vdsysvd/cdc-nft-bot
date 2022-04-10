package it.nftspace.cdcnftbot.client;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.PostConstruct;

@Component
@RequiredArgsConstructor
public class AppScheduler {

    private final RestClientService restClientService;
    public static final Set<String> CACHE = new HashSet<>();

    @Scheduled(fixedDelay = 10000)
    public void run(){
        restClientService.auction();
    }

    @Scheduled(fixedDelay = 10000)
    public void buyNow(){
        restClientService.buyNow();
    }

    @Scheduled(fixedDelay = 60*60 * 1000)
    public void cache(){
        CACHE.clear();
    }

}
