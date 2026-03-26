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

import org.springframework.beans.factory.annotation.Value;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
public class YouTubeService {

    @Value("${huggingface.apiKey:}")
    private String hfApiKey;

    private final YouTubeClient client;
    private final VideoRepo repo;

    public void fetch(String keyword){
        Map res = client.search(keyword);
        List<Map> items = (List<Map>) res.get("items");
        List<String> ids = new ArrayList<>();

        for(Map item : items){
            Map idMap = (Map) item.get("id");
            ids.add((String) idMap.get("videoId"));
        }

        Map details = client.getVideoDetails(String.join(",", ids));
        List<Map> videos = (List<Map>) details.get("items");

        for(Map v : videos){
            String id = (String) v.get("id");
            Map snippet = (Map) v.get("snippet");
            Map stats = (Map) v.get("statistics");
            Map content = (Map) v.get("contentDetails");

            String publishedAt = (String) snippet.get("publishedAt");
            Instant instant = Instant.parse(publishedAt);
            int videoYear = instant.atZone(ZoneId.systemDefault()).getYear();
            int currentYear = java.time.Year.now().getValue();

            if(videoYear != currentYear) continue; // Skip if not current year

            Video video = new Video();
            video.setId(id);
            video.setTitle((String) snippet.get("title"));
            video.setChannel((String) snippet.get("channelTitle"));
            video.setViews(Long.parseLong((String) stats.getOrDefault("viewCount", "0")));
            video.setDuration(java.time.Duration.parse((String) content.get("duration")).getSeconds());
            video.setPublishedAt(publishedAt);
            video.setPublishedDateFormatted(instant.atZone(ZoneId.systemDefault())
                    .format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")));

            video.setKeyword(keyword);
            video.setContentType(video.getDuration() <= 60 ? "Short" : "Video");
            
            try {
                Map thumbnails = (Map) snippet.get("thumbnails");
                if (thumbnails != null && thumbnails.get("high") != null) {
                    video.setThumbnailUrl((String) ((Map) thumbnails.get("high")).get("url"));
                }
            } catch (Exception e) {}

            try {
                String videoDesc = (String) snippet.getOrDefault("description", "");
                
                ProcessBuilder pb = new ProcessBuilder("python", "scripts/youtube_analyzer.py", 
                    id, video.getTitle(), videoDesc, hfApiKey);
                pb.redirectErrorStream(true);
                Process process = pb.start();
                
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                StringBuilder output = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line);
                }
                process.waitFor();
                
                String jsonOutput = output.toString();
                if (jsonOutput.contains("{")) {
                    jsonOutput = jsonOutput.substring(jsonOutput.indexOf("{"));
                    ObjectMapper mapper = new ObjectMapper();
                    Map<String, String> analysis = mapper.readValue(jsonOutput, Map.class);
                    video.setSentiment(analysis.getOrDefault("sentiment", "neutral"));
                    video.setSummary(analysis.getOrDefault("summary", ""));
                } else {
                    video.setSentiment("neutral");
                    video.setSummary("Analysis failed: " + jsonOutput);
                }
            } catch (Exception e) {
                e.printStackTrace();
                video.setSentiment("neutral");
                video.setSummary("Error executing analyzer");
            }

            repo.save(video);
        }
    }
}