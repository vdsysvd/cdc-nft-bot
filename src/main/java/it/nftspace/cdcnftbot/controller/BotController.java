package it.nftspace.cdcnftbot.controller;

import it.nftspace.cdcnftbot.config.CdcCollection;
import it.nftspace.cdcnftbot.service.BotService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController @RequiredArgsConstructor public class BotController {

    private final BotService botService;

    @GetMapping("/collection/{cName}") public void bestRanking(@PathVariable CdcCollection cName,
        @RequestParam(required = false, defaultValue = "1000") int maxPrice,
        @RequestParam(required = false, defaultValue = "10") int limit,
        @RequestParam(required = false, defaultValue = "false") boolean orderByPrice,
        @RequestParam(required = false, defaultValue = "false") boolean auction,
        @RequestParam(required = false, defaultValue = "1") int spread,
        @RequestParam(required = false, defaultValue = "1") int hours) {

        if (auction) {
            botService.auction(cName.toString(), hours, spread, limit);
        } else {
            botService.topListed(cName, maxPrice, limit, orderByPrice);

        }
    }

}
