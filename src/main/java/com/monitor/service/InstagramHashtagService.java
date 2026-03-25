package com.monitor.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.monitor.model.InstagramHashtagCache;
import com.monitor.model.dto.HashtagAnalysisResponse;
import com.monitor.repo.InstagramHashtagCacheRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class InstagramHashtagService {

    @Autowired
    private InstagramHashtagCacheRepo cacheRepo;

    public HashtagAnalysisResponse analyzeHashtag(String hashtag) throws Exception {
        // Check cache in database
        Optional<InstagramHashtagCache> optionalCache = cacheRepo.findById(hashtag);
        ObjectMapper mapper = new ObjectMapper();

        if (optionalCache.isPresent()) {
            InstagramHashtagCache cache = optionalCache.get();
            
            if (cache.getJsonResponse().contains("\"error\"")) {
                // Enforce 15-minute cool-down if previous scrape failed
                if (cache.getUpdatedAt().plusMinutes(15).isAfter(LocalDateTime.now())) {
                    long minutesLeft = java.time.Duration.between(LocalDateTime.now(), cache.getUpdatedAt().plusMinutes(15)).toMinutes();
                    throw new RuntimeException("Cool-down active against Instagram rate-limit blocks. Try again in " + minutesLeft + " minutes.");
                }
            } else {
                // Cache is valid for 10 minutes for success responses
                if (cache.getUpdatedAt().plusMinutes(10).isAfter(LocalDateTime.now())) {
                    return mapper.readValue(cache.getJsonResponse(), HashtagAnalysisResponse.class);
                }
            }
        }

        // Execute Python Script
        String scriptPath = "scripts/instagram_analyzer.py";
        ProcessBuilder processBuilder = new ProcessBuilder("python", scriptPath, hashtag);
        // The working directory is the project root when launched via java -jar or mvn
        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        StringBuilder output = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            output.append(line);
        }

        int exitCode = process.waitFor();
        String jsonResponse = output.toString();

        if (exitCode != 0 || jsonResponse.contains("\"error\"")) {
            // Save error into the cache DB to activate the 15 minute cool-down
            InstagramHashtagCache errorCache = optionalCache.orElse(new InstagramHashtagCache());
            errorCache.setHashtag(hashtag);
            errorCache.setJsonResponse(jsonResponse.isEmpty() ? "{\"error\":\"Native scraping exception\"}" : jsonResponse);
            errorCache.setUpdatedAt(LocalDateTime.now());
            cacheRepo.save(errorCache);
            
            throw new RuntimeException("Error executing Python scraping script: " + jsonResponse);
        }

        // Save or update cache in database
        InstagramHashtagCache newCache = optionalCache.orElse(new InstagramHashtagCache());
        newCache.setHashtag(hashtag);
        newCache.setJsonResponse(jsonResponse);
        newCache.setUpdatedAt(LocalDateTime.now());
        cacheRepo.save(newCache);

        return mapper.readValue(jsonResponse, HashtagAnalysisResponse.class);
    }
}
