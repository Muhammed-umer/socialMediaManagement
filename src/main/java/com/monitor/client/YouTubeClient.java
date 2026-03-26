package com.monitor.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import java.util.Map;

@Component
public class YouTubeClient {

    @Value("${youtube.apiKey}")
    private String apiKey;

    private final RestTemplate rest = new RestTemplate();

    public Map search(String keyword){
        String url = "https://www.googleapis.com/youtube/v3/search"
                + "?part=snippet&type=video&maxResults=10"
                + "&q=" + keyword
                + "&key=" + apiKey;
        return rest.getForObject(url, Map.class);
    }

    public Map getVideoDetails(String ids){
        String url = "https://www.googleapis.com/youtube/v3/videos"
                + "?part=contentDetails,statistics,snippet"
                + "&id=" + ids
                + "&key=" + apiKey;
        return rest.getForObject(url, Map.class);
    }
}