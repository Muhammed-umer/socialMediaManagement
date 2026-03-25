package com.monitor.model.dto;

import lombok.Data;
import java.util.List;

@Data
public class HashtagAnalysisResponse {
    private String hashtag;
    private int total_posts;
    private AnalysisDTO analysis;
    private List<PostDTO> posts;
    private List<TopAccountDTO> top_accounts;

    @Data
    public static class AnalysisDTO {
        private int positive;
        private int negative;
        private int neutral;
    }

    @Data
    public static class PostDTO {
        private String username;
        private String caption;
        private String sentiment;
        private int likes;
        private String url;
    }

    @Data
    public static class TopAccountDTO {
        private String username;
        private int post_count;
    }
}
