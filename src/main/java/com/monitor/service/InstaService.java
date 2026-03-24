package com.monitor.service;

import com.monitor.model.Insta;
import com.monitor.repo.InstaRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
public class InstaService {

    private final InstaRepo repo;
    private final com.monitor.client.InstaClient client;

    public void fetch(String keywordInput) {
        String[] keywords = keywordInput.split(",");

        for (String rawKeyword : keywords) {
            String keyword = rawKeyword.trim().toLowerCase();
            if (keyword.isEmpty()) continue;

            // Business Discovery: Fetch Media from a specific public username
            List<java.util.Map<String, Object>> mediaItems = client.getBusinessDiscoveryMedia(keyword);

            if (mediaItems.isEmpty()) {
                System.out.println("No posts found or account is not a public business profile: " + keyword);
                continue;
            }

            for (java.util.Map<String, Object> item : mediaItems) {
                String id = (String) item.get("id");
                String caption = (String) item.getOrDefault("caption", "No Caption");
                String type = (String) item.get("media_type"); // IMAGE, VIDEO, or CAROUSEL_ALBUM
                long likes = Long.parseLong(String.valueOf(item.getOrDefault("like_count", 0)));
                long comments = Long.parseLong(String.valueOf(item.getOrDefault("comments_count", 0)));
                String mediaUrl = (String) item.get("media_url");
                String permalink = (String) item.get("permalink");
                String timestamp = (String) item.get("timestamp"); // ISO format

                // Format timestamp for display
                String formattedDate = timestamp;
                try {
                    java.time.Instant instant = java.time.Instant.parse(timestamp);
                    formattedDate = instant
                            .atZone(java.time.ZoneId.systemDefault())
                            .format(java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));
                } catch (Exception e) {}

                Insta post = new Insta();
                post.setId(id);
                post.setTitle(caption.length() > 100 ? caption.substring(0, 97) + "..." : caption);
                post.setAccount("@" + keyword); 
                post.setContentType(type.equals("VIDEO") ? "Reel" : "Post");
                post.setLikes(likes);
                post.setComments(comments);
                post.setPublishedDateFormatted(formattedDate);
                post.setKeyword(keyword);
                post.setThumbnailUrl(mediaUrl != null ? mediaUrl : "");
                post.setPostUrl(permalink);
                
                repo.save(post);
            }
        }
    }
}
