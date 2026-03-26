package com.monitor.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;

@Entity
@Table(name = "twitter_post")
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

    public TwitterPost() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getTweetText() { return tweetText; }
    public void setTweetText(String tweetText) { this.tweetText = tweetText; }
    public int getLikes() { return likes; }
    public void setLikes(int likes) { this.likes = likes; }
    public int getComments() { return comments; }
    public void setComments(int comments) { this.comments = comments; }
    public int getRetweets() { return retweets; }
    public void setRetweets(int retweets) { this.retweets = retweets; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public String getSentiment() { return sentiment; }
    public void setSentiment(String sentiment) { this.sentiment = sentiment; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
}
