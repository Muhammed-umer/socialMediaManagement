package com.monitor.controller;

import com.monitor.model.dto.HashtagAnalysisResponse;
import com.monitor.service.InstagramHashtagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/instagram/hashtag")
@CrossOrigin("*")
public class InstagramHashtagController {

    @Autowired
    private InstagramHashtagService instagramHashtagService;

    @GetMapping("/{tag}")
    public ResponseEntity<?> analyzeHashtag(@PathVariable String tag) {
        try {
            HashtagAnalysisResponse response = instagramHashtagService.analyzeHashtag(tag);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            String errorMsg = e.getMessage().replace("\"", "\\\"");
            return ResponseEntity.internalServerError().body("{\"error\": \"" + errorMsg + "\"}");
        }
    }
}
