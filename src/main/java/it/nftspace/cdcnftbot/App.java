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

    public static final Map<String, String> ALPHA_BOT;
    static {
        try {
            ALPHA_BOT = alphabotRanking();
        } catch (IOException e) {
            throw new IllegalStateException();
        }
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
