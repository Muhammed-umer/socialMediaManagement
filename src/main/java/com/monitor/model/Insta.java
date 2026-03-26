package com.monitor.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class Insta {
    @Id
    private String id;

    private String title;
    private String account;
    private String contentType; 

    private Long likes;
    private Long comments;
    private String publishedDateFormatted;

    private String keyword;
    private String thumbnailUrl;
    private String postUrl;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getAccount() { return account; }
    public void setAccount(String account) { this.account = account; }
    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }
    public Long getLikes() { return likes; }
    public void setLikes(Long likes) { this.likes = likes; }
    public Long getComments() { return comments; }
    public void setComments(Long comments) { this.comments = comments; }
    public String getPublishedDateFormatted() { return publishedDateFormatted; }
    public void setPublishedDateFormatted(String publishedDateFormatted) { this.publishedDateFormatted = publishedDateFormatted; }
    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }
    public String getThumbnailUrl() { return thumbnailUrl; }
    public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }
    public String getPostUrl() { return postUrl; }
    public void setPostUrl(String postUrl) { this.postUrl = postUrl; }
}
