package com.monitor.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class Insta {
    @Id
    private String id;

    private String title;
    private String account;
    private String contentType; // "Reel" or "Post"

    private Long likes;
    private Long comments;
    private String publishedDateFormatted;

    private String keyword;
    private String thumbnailUrl;
    private String postUrl;
}
