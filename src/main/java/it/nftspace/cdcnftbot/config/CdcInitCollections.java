package it.nftspace.cdcnftbot.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.nftspace.cdcnftbot.App;
import lombok.Data;
import org.springframework.boot.SpringApplication;
import org.springframework.core.io.ClassPathResource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class CdcInitCollections {

    public static final Map<String, Map<String, String>> COLLECTIONS = new TreeMap<>();
    static {
        Map<String, String> alphaBots = null;
        Map<String, String> alphaBotsWeapons = null;
        Map<String, String> ballies = null;
        Map<String, String> ll = null;
        Map<String, String> psychoKitties = null;
        Map<String, String> psychoMollies = null;
        Map<String, String> madHares = null;
        try {
            alphaBots = alphabotRanking("alphabot.txt", 0);
            alphaBotsWeapons = alphabotRanking("alphabotweapon.txt", 2);
            ll = llRanking();
            psychoKitties = getMapCollection("psychokitties.txt");
            psychoMollies = getMapCollection("psychomollies.txt");
            madHares = getMapCollection("madhares.txt");
        } catch (IOException e) {
            //ignore
        }
        COLLECTIONS.put(CdcCollection.ALPHA_BOTS.toString(), alphaBots);
        COLLECTIONS.put(CdcCollection.ALPHA_BOTS_WEAPONS.toString(), alphaBotsWeapons);
        COLLECTIONS.put(CdcCollection.BALLIES.toString(), ballies);
        COLLECTIONS.put(CdcCollection.LL.toString(), ll);
        COLLECTIONS.put(CdcCollection.PSYCHO_KITTIES.toString(), psychoKitties);
        COLLECTIONS.put(CdcCollection.PSYCHO_MOLLIES.toString(), psychoMollies);
        COLLECTIONS.put(CdcCollection.MAD_HARES.toString(), madHares);
    }
    private static Map<String, String> alphabotRanking(String fileName, int skipLineNo) throws IOException {
        Map<String, String> map = new TreeMap<>();
        var inputStream = new ClassPathResource(fileName).getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        int skipped = 0;
        while(reader.ready()) {
            if(skipped < skipLineNo){
                skipped ++;
                continue;
            }
            skipped = 0;
            String line = reader.readLine();
            String[] info = line.split("\t");
            map.put(info[0], info[3]);
        }
        return map;
    }
    private static Map<String, String> llRanking() throws IOException {
        Map<String, String> map = new TreeMap<>();
        ObjectMapper objectMapper = new ObjectMapper();
        var lions = objectMapper.readValue(new ClassPathResource("loadedlion.json").getFile(), LLions.class);
        for(var l : lions.getLions()) {
            map.put("#" + l.getId(), String.valueOf(l.getRank()));
        }
        return map;
    }
    private static Map<String, String> getMapCollection(String fileName) throws IOException {
        Map<String, String> map = new TreeMap<>();
        var inputStream = new ClassPathResource(fileName).getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        while(reader.ready()) {
            String line = reader.readLine();
            String[] info = line.split("\t");
            map.put("#" + info[1], info[0]);
        }
        return map;
    }

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }


    @JsonIgnoreProperties(ignoreUnknown = true)
    @Data public static class LLions {
        private List<LL> lions;
    }
    @JsonIgnoreProperties(ignoreUnknown = true)
    @Data public static class LL {
        private String id;
        private int rank;
    }

}
