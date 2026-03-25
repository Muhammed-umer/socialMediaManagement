package com.monitor.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Tweet {
    private String username;
    private String text;
    private int likes;
    private int comments;
    private int retweets;
    private String createdAt;
    private String source;
    private String sentiment;
}
