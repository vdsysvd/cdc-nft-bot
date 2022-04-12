package it.nftspace.cdcnftbot.config;

import it.nftspace.cdcnftbot.service.BotService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class AppSchedulerConfig {

    private final BotService botService;
    public static final Set<String> CACHE = new HashSet<>();

    @Scheduled(cron = "${jobs.bot.run_schedule:-}")
    public void runBot(){
        System.out.println("executing bot");
        botService.auction(CdcCollection.ALPHA_BOTS.toString(), 1, 50, 100);
        botService.buyNow(CdcCollection.ALPHA_BOTS.toString(), 420, 100);
        botService.auction(CdcCollection.LL.toString(), 1, 300, 100);
        botService.buyNow(CdcCollection.LL.toString(), 3900, 100);
    }

    @Scheduled(cron = "${jobs.bot.run_schedule:-}")
    public void cache(){
        CACHE.clear();
    }

}
