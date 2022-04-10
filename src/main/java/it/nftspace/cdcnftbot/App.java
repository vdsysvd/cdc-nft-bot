package it.nftspace.cdcnftbot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.TreeMap;

@EnableScheduling
@SpringBootApplication public class App {

    public static final Map<String, Map<String, String>> COLLECTIONS = new TreeMap<>();;
    static {
        Map<String, String> alphaBots = null;
        Map<String, String> ballies = null;
        try {
            alphaBots = alphabotRanking();
        } catch (IOException e) {
            //ignore
        }
        COLLECTIONS.put("4ff90f089ac3ef9ce342186adc48a30d", alphaBots);
        COLLECTIONS.put("6c7b1a68479f2fc35e9f81e42bcb7397", ballies);
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

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

}
