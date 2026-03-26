package com.monitor.model.dto;

import java.util.List;

public class HashtagAnalysisResponse {
    private String hashtag;
    private int total_posts;
    private AnalysisDTO analysis;
    private List<PostDTO> posts;
    private List<TopAccountDTO> top_accounts;

    public String getHashtag() { return hashtag; }
    public void setHashtag(String hashtag) { this.hashtag = hashtag; }
    public int getTotal_posts() { return total_posts; }
    public void setTotal_posts(int total_posts) { this.total_posts = total_posts; }
    public AnalysisDTO getAnalysis() { return analysis; }
    public void setAnalysis(AnalysisDTO analysis) { this.analysis = analysis; }
    public List<PostDTO> getPosts() { return posts; }
    public void setPosts(List<PostDTO> posts) { this.posts = posts; }
    public List<TopAccountDTO> getTop_accounts() { return top_accounts; }
    public void setTop_accounts(List<TopAccountDTO> top_accounts) { this.top_accounts = top_accounts; }

    public static class AnalysisDTO {
        private int positive;
        private int negative;
        private int neutral;

        public int getPositive() { return positive; }
        public void setPositive(int positive) { this.positive = positive; }
        public int getNegative() { return negative; }
        public void setNegative(int negative) { this.negative = negative; }
        public int getNeutral() { return neutral; }
        public void setNeutral(int neutral) { this.neutral = neutral; }
    }

    public static class PostDTO {
        private String username;
        private String caption;
        private String sentiment;
        private int likes;
        private int comments;
        private String type;
        private String timestamp;
        private String url;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getCaption() { return caption; }
        public void setCaption(String caption) { this.caption = caption; }
        public String getSentiment() { return sentiment; }
        public void setSentiment(String sentiment) { this.sentiment = sentiment; }
        public int getLikes() { return likes; }
        public void setLikes(int likes) { this.likes = likes; }
        public int getComments() { return comments; }
        public void setComments(int comments) { this.comments = comments; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getTimestamp() { return timestamp; }
        public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
    }

    public static class TopAccountDTO {
        private String username;
        private int post_count;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public int getPost_count() { return post_count; }
        public void setPost_count(int post_count) { this.post_count = post_count; }
    }
}
