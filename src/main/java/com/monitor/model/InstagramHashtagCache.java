package com.monitor.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
public class InstagramHashtagCache {
    @Id
    @Column(unique = true)
    private String hashtag;
    
    @Column(columnDefinition = "TEXT")
    private String jsonResponse;
    
    private LocalDateTime updatedAt;
}
