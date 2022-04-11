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
        Map<String, String> ballies = null;
        Map<String, String> ll = null;
        Map<String, String> psycoKitties = null;
        try {
            alphaBots = alphabotRanking();
            ll = llRanking();
        } catch (IOException e) {
            //ignore
        }
        COLLECTIONS.put("4ff90f089ac3ef9ce342186adc48a30d", alphaBots);
        COLLECTIONS.put("6c7b1a68479f2fc35e9f81e42bcb7397", ballies);
        COLLECTIONS.put("82421cf8e15df0edcaa200af752a344f", ll);
        COLLECTIONS.put("faa3d8da88f9ee2f25267e895db71471", psycoKitties);
    }
    private static Map<String, String> alphabotRanking() throws IOException {
        Map<String, String> map = new TreeMap<>();
        var inputStream = new ClassPathResource("alphabot.txt").getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        while(reader.ready()) {
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
