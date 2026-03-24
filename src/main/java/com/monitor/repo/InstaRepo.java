package com.monitor.repo;

import com.monitor.model.Insta;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InstaRepo extends JpaRepository<Insta, String> {
}
