package com.monitor.repo;

import com.monitor.model.InstagramHashtagCache;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InstagramHashtagCacheRepo extends JpaRepository<InstagramHashtagCache, String> {
}
