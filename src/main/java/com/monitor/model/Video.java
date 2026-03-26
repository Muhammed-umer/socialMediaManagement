package com.monitor.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class Video {
    @Id
    private String id;
    private String title;
    private String channel;
    private Long views;
    private Long duration; // stored in seconds
    private String publishedAt;
    private String publishedDateFormatted;

    private String keyword;
    private String sentiment;
    @jakarta.persistence.Column(columnDefinition = "TEXT")
    private String summary;

    private String contentType;
    private String thumbnailUrl;
}