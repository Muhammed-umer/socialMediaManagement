package com.monitor.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

import com.monitor.model.dto.HashtagAnalysisResponse;
import com.monitor.model.dto.HashtagAnalysisResponse.PostDTO;
import com.monitor.model.dto.HashtagAnalysisResponse.AnalysisDTO;
import com.monitor.model.dto.HashtagAnalysisResponse.TopAccountDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class InstagramHashtagService {

    @Value("${instagram.apiKey}")
    private String apiKey;

    @Value("${instagram.host}")
    private String apiHost;

    private final RestTemplate restTemplate = new RestTemplate();

    public HashtagAnalysisResponse fetchByHashtag(String hashtag) {

        System.out.println("Fetching Instagram posts for hashtag: " + hashtag);

        String url = "https://instagram-looter2.p.rapidapi.com/tag-feeds?query=" + hashtag;

        HttpHeaders headers = new HttpHeaders();
        headers.set("x-rapidapi-key", apiKey);
        headers.set("x-rapidapi-host", apiHost);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> responseEntity = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            String body = responseEntity.getBody();
            System.out.println("Instagram API Response: " + body);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(body);

            List<PostDTO> posts = new ArrayList<>();
            Map<String, Integer> accountCounts = new HashMap<>();

            JsonNode edges = root
                    .path("data")
                    .path("hashtag")
                    .path("edge_hashtag_to_media")
                    .path("edges");

            System.out.println("Edges size: " + edges.size());

            for (int i = 0; i < edges.size(); i++) {
                try {
                    JsonNode node = edges.get(i).path("node");

                    String displayUrl = node.path("display_url").asText("");

                    if (displayUrl == null || displayUrl.isEmpty()) {
                        continue;
                    }

                    PostDTO dto = new PostDTO();

                    // CAPTION
                    String caption = "";
                    JsonNode captionEdges = node.path("edge_media_to_caption").path("edges");
                    if (captionEdges.size() > 0) {
                        caption = captionEdges.get(0)
                                .path("node")
                                .path("text")
                                .asText("");
                    }
                    dto.setCaption(caption);

                    // FIXED LIKES
                    dto.setLikes(node.path("edge_media_preview_like").path("count").asInt(0));

                    // COMMENTS
                    dto.setComments(node.path("edge_media_to_comment").path("count").asInt(0));

                    // FIXED TYPE
                    boolean isVideo = node.path("is_video").asBoolean(false);
                    dto.setType(isVideo ? "REEL" : "POST");

                    // TIMESTAMP
                    dto.setTimestamp(node.path("taken_at_timestamp").asText(""));

                    // USER
                    String username = node.path("owner").path("id").asText("unknown");
                    dto.setUsername(username);

                    dto.setUrl(displayUrl);
                    dto.setSentiment("neutral");

                    posts.add(dto);
                    accountCounts.put(username, accountCounts.getOrDefault(username, 0) + 1);

                } catch (Exception e) {
                    System.out.println("Error parsing node: " + e.getMessage());
                }
            }

            System.out.println("Posts parsed: " + posts.size());

            HashtagAnalysisResponse result = new HashtagAnalysisResponse();
            result.setHashtag(hashtag);
            result.setTotal_posts(posts.size());
            result.setPosts(posts);

            AnalysisDTO analysis = new AnalysisDTO();
            analysis.setNeutral(posts.size());
            analysis.setPositive(0);
            analysis.setNegative(0);
            result.setAnalysis(analysis);

            List<TopAccountDTO> topAccounts = accountCounts.entrySet().stream()
                    .map(e -> {
                        TopAccountDTO t = new TopAccountDTO();
                        t.setUsername(e.getKey());
                        t.setPost_count(e.getValue());
                        return t;
                    })
                    .sorted((a, b) -> b.getPost_count() - a.getPost_count())
                    .limit(5)
                    .collect(Collectors.toList());
            result.setTop_accounts(topAccounts);

            return result;

        } catch (Exception e) {
            System.out.println("Instagram API Error: " + e.getMessage());
            return new HashtagAnalysisResponse();
        }
    }
}
