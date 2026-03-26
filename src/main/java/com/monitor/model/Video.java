package com.monitor.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class Video {
    @Id
    private String id;
    private String title;
    private String channel;
<<<<<<< Updated upstream
    private Long views;
    private Long duration; // stored in seconds
=======
    private String contentType; 

    private Long views;
    private Long duration; 
>>>>>>> Stashed changes
    private String publishedAt;
    private String publishedDateFormatted;

    private String keyword;
    private String sentiment;
    @jakarta.persistence.Column(columnDefinition = "TEXT")
    private String summary;

    private String contentType;
    private String thumbnailUrl;
<<<<<<< Updated upstream
}
=======

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }
    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }
    public Long getViews() { return views; }
    public void setViews(Long views) { this.views = views; }
    public Long getDuration() { return duration; }
    public void setDuration(Long duration) { this.duration = duration; }
    public String getPublishedAt() { return publishedAt; }
    public void setPublishedAt(String publishedAt) { this.publishedAt = publishedAt; }
    public String getPublishedDateFormatted() { return publishedDateFormatted; }
    public void setPublishedDateFormatted(String publishedDateFormatted) { this.publishedDateFormatted = publishedDateFormatted; }
    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }
    public String getThumbnailUrl() { return thumbnailUrl; }
    public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }
}
>>>>>>> Stashed changes
