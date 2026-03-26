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
    private String contentType; // "Video" or "Short"

    private Long views;
    private Long duration; // seconds
    private String publishedAt;
    private String publishedDateFormatted;

    /** The search keyword that was used to fetch this video */
    private String keyword;

    private String thumbnailUrl;

    @jakarta.persistence.Column(columnDefinition = "TEXT")
    private String aiSummary;
    private String sentiment; // Positive, Negative, Neutral
    private String publishedAgo; // HH:MM:SS ago

}
