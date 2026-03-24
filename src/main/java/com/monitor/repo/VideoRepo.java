package com.monitor.repo;

import com.monitor.model.Video;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VideoRepo extends JpaRepository<Video, String> {
    List<Video> findByKeywordIgnoreCase(String keyword);
}
