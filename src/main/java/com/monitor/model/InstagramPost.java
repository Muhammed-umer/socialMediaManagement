package com.monitor.model;

import jakarta.persistence.*;

@Entity
@Table(name = "instagram_post")
public class InstagramPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    
    @Column(columnDefinition = "TEXT")
    private String caption;

    private int likes;
    private String mediaUrl;
    
    @Column(unique = true)
    private String postUrl;
    
    private String timestamp;

    public InstagramPost() {}

    public InstagramPost(String username, String caption, int likes, String mediaUrl, String postUrl, String timestamp) {
        this.username = username;
        this.caption = caption;
        this.likes = likes;
        this.mediaUrl = mediaUrl;
        this.postUrl = postUrl;
        this.timestamp = timestamp;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getCaption() { return caption; }
    public void setCaption(String caption) { this.caption = caption; }
    public int getLikes() { return likes; }
    public void setLikes(int likes) { this.likes = likes; }
    public String getMediaUrl() { return mediaUrl; }
    public void setMediaUrl(String mediaUrl) { this.mediaUrl = mediaUrl; }
    public String getPostUrl() { return postUrl; }
    public void setPostUrl(String postUrl) { this.postUrl = postUrl; }
    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
}
