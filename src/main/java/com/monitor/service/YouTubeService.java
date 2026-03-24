package com.monitor.service;

import com.monitor.client.YouTubeClient;
import com.monitor.model.Video;
import com.monitor.repo.VideoRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class YouTubeService {

    private final YouTubeClient client;
    private final VideoRepo repo;



    public void fetch(String keywordInput){

        String[] keywords = keywordInput.split(",");

        for(String rawKeyword : keywords) {
            String keyword = rawKeyword.trim();
            if(keyword.isEmpty()) continue;

            Map<String, Object> res = client.search(keyword);
            List<Map<String, Object>> items = (List<Map<String, Object>>) res.get("items");

            List<String> ids = new ArrayList<>();

            for(Map<String, Object> item : items){
                Map<String, Object> idMap = (Map<String, Object>) item.get("id");
                ids.add((String) idMap.get("videoId"));
            }

            Map<String, Object> details = client.getVideoDetails(String.join(",", ids));
            List<Map<String, Object>> videos = (List<Map<String, Object>>) details.get("items");

            for(Map<String, Object> v : videos){


                String id = (String) v.get("id");

                Map<String, Object> snippet = (Map<String, Object>) v.get("snippet");
                Map<String, Object> stats = (Map<String, Object>) v.get("statistics");
                Map<String, Object> content = (Map<String, Object>) v.get("contentDetails");

                String title = (String) snippet.get("title");
                String channel = (String) snippet.get("channelTitle");
                String publishedAt = (String) snippet.get("publishedAt");
                String formattedDate = "";

                try {
                    Instant instant = Instant.parse(publishedAt);

                    // include seconds in published timestamp
                    formattedDate = instant
                            .atZone(ZoneId.systemDefault())
                            .format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));

                } catch (Exception e) {
                    formattedDate = publishedAt; // fallback
                }
                long views = Long.parseLong((String) stats.getOrDefault("viewCount", "0"));

                String durationStr = (String) content.get("duration");
                long seconds = java.time.Duration.parse(durationStr).getSeconds();
                int currentYear = java.time.Year.now().getValue();

                Instant instant = Instant.parse(publishedAt);

                int videoYear = instant
                        .atZone(ZoneId.systemDefault())
                        .getYear();

                if(videoYear != currentYear){
                    continue; // ❌ skip old videos
                }
                Map<String, Object> thumbnails = (Map<String, Object>) snippet.get("thumbnails");
                Map<String, Object> high = (Map<String, Object>) thumbnails.get("high");
                String thumbUrl = (high != null) ? (String) high.get("url") : "";

                Video video = new Video();
                video.setId(id);
                video.setTitle(title);
                video.setChannel(channel);
                video.setContentType(seconds <= 60 ? "Short" : "Video");
                video.setViews(views);
                video.setDuration(seconds);
                video.setPublishedAt(publishedAt);
                video.setPublishedDateFormatted(formattedDate);
                video.setKeyword(keyword);
                video.setThumbnailUrl(thumbUrl);

                repo.save(video);
            }
        }
    }
}
