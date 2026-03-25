package com.monitor.repository;

import com.monitor.model.TwitterPost;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TwitterPostRepository extends JpaRepository<TwitterPost, Long> {
    List<TwitterPost> findTop10ByOrderByIdDesc();
    List<TwitterPost> findTop20ByOrderByIdDesc();
}
