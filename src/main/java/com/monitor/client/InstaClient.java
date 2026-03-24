package com.monitor.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Component
public class InstaClient {

    @Value("${instagram.accessToken}")
    private String accessToken;

    @Value("${instagram.businessAccountId}")
    private String businessAccountId;

    private final RestTemplate rest = new RestTemplate();

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getBusinessDiscoveryMedia(String username) {
        String url = "https://graph.facebook.com/v19.0/" + businessAccountId
                + "?fields=business_discovery.username(" + username + "){media{id,caption,media_type,media_url,permalink,like_count,comments_count,timestamp}}"
                + "&access_token=" + accessToken;

        try {
            Map<String, Object> response = rest.getForObject(url, Map.class);
            System.out.println("Instagram API Response for @" + username + ": " + response);
            
            if (response != null && response.containsKey("business_discovery")) {
                Map<String, Object> discovery = (Map<String, Object>) response.get("business_discovery");
                if (discovery != null && discovery.containsKey("media")) {
                    Map<String, Object> media = (Map<String, Object>) discovery.get("media");
                    if (media != null && media.containsKey("data")) {
                        return (List<Map<String, Object>>) media.get("data");
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Instagram Business Discovery Error for @" + username + ": " + e.getMessage());
        }
        return List.of();
    }
}
