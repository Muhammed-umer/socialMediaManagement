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
    private final AIService aiService;

    public void fetch(String keywordInput){

        String[] keywords = keywordInput.split(",");

        for(String rawKeyword : keywords) {
            String keyword = rawKeyword.trim();
            if(keyword.isEmpty()) continue;

            // Adding Erode context to the search naturally
            String refinedKeyword = keyword + " Erode election district politics";

            Map<String, Object> res = (Map<String, Object>) client.search(refinedKeyword);
            List<Map<String, Object>> items = (List<Map<String, Object>>) res.get("items");

            if (items == null) continue;

            List<String> ids = new ArrayList<>();
            for(Map<String, Object> item : items){
                Map<String, Object> idMap = (Map<String, Object>) item.get("id");
                if (idMap != null && idMap.get("videoId") != null) {
                    ids.add((String) idMap.get("videoId"));
                }
            }

            if (ids.isEmpty()) continue;

            Map<String, Object> details = (Map<String, Object>) client.getVideoDetails(String.join(",", ids));
            List<Map<String, Object>> videosData = (List<Map<String, Object>>) details.get("items");

            if (videosData == null) continue;

            for(Map<String, Object> v : videosData){
                String id = (String) v.get("id");

                Map<String, Object> snippet = (Map<String, Object>) v.get("snippet");
                Map<String, Object> stats = (Map<String, Object>) v.get("statistics");
                Map<String, Object> content = (Map<String, Object>) v.get("contentDetails");

                String title = (String) snippet.get("title");
                String channel = (String) snippet.get("channelTitle");
                String publishedAt = (String) snippet.get("publishedAt");
                
                // Calculate relative time: HH:MM:SS ago
                String publishedAgoStr = calculatePublishedAgo(publishedAt);

                String formattedDate = "";
                try {
                    Instant instant = Instant.parse(publishedAt);
                    formattedDate = instant
                            .atZone(ZoneId.systemDefault())
                            .format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));
                } catch (Exception e) {
                    formattedDate = publishedAt;
                }
                
                long views = 0;
                try {
                    views = Long.parseLong((String) stats.getOrDefault("viewCount", "0"));
                } catch (Exception ignored) {}

                String durationStr = (String) content.get("duration");
                long seconds = 0;
                try {
                    seconds = java.time.Duration.parse(durationStr).getSeconds();
                } catch (Exception ignored) {}

                int currentYear = java.time.Year.now().getValue();
                Instant instantObj = Instant.parse(publishedAt);
                int videoYear = instantObj.atZone(ZoneId.systemDefault()).getYear();

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
                video.setPublishedAgo(publishedAgoStr);
                video.setKeyword(keyword);
                video.setThumbnailUrl(thumbUrl);

                // HuggingFace AI integration
                try {
                    Map<String, String> analysis = aiService.analyze(title + " " + channel + " content relating to elections and politics");
                    video.setAiSummary(analysis.get("aiSummary"));
                    video.setSentiment(analysis.get("sentiment"));
                } catch (Exception e) {
                    video.setAiSummary("AI Summary failed to generate.");
                    video.setSentiment("Neutral");
                }

                repo.save(video);
            }
        }
    }

    private String calculatePublishedAgo(String publishedAt) {
        try {
            Instant publishedInstant = Instant.parse(publishedAt);
            Instant now = Instant.now();
            java.time.Duration diff = java.time.Duration.between(publishedInstant, now);
            
            long hours = diff.toHours();
            long minutes = diff.toMinutesPart();
            long seconds = diff.toSecondsPart();
            
            return String.format("%02d:%02d:%02d ago", hours, minutes, seconds);
        } catch (Exception e) {
            return "N/A ago";
        }
    }
}
