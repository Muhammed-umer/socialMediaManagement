package com.monitor.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "twitter_post")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TwitterPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    
    @Column(columnDefinition = "TEXT")
    private String tweetText;

    private int likes;
    private int comments;
    private int retweets;
    private String createdAt;
    private String sentiment;
    private String source;
}
