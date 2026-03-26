package com.monitor.repository;

import com.monitor.model.InstagramPost;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface InstagramPostRepository extends JpaRepository<InstagramPost, Long> {
    Optional<InstagramPost> findByPostUrl(String postUrl);
}
