package com.monitor.controller;

import com.monitor.model.dto.HashtagAnalysisResponse;
import com.monitor.service.InstagramHashtagService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/instagram/hashtag")
@CrossOrigin("*")
public class InstagramHashtagController {

    private final InstagramHashtagService instagramHashtagService;

    public InstagramHashtagController(InstagramHashtagService instagramHashtagService) {
        this.instagramHashtagService = instagramHashtagService;
    }

    @GetMapping("/{tag}")
    public ResponseEntity<?> analyzeHashtag(@PathVariable String tag) {
        try {
            com.monitor.model.dto.HashtagAnalysisResponse result = instagramHashtagService.fetchByHashtag(tag);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("hashtag", tag);
            
            if (result == null || result.getPosts() == null || result.getPosts().isEmpty()) {
                response.put("message", "No posts found for this hashtag");
                response.put("posts", List.of());
                response.put("totalPosts", 0);
            } else {
                response.put("totalPosts", result.getTotal_posts());
                response.put("posts", result.getPosts());
                response.put("analysis", result.getAnalysis());
                response.put("topAccounts", result.getTop_accounts());
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
}
