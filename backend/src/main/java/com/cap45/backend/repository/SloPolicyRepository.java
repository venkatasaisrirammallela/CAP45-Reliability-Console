package com.cap45.backend.repository;

import com.cap45.backend.entity.SloPolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.time.Instant;
public interface SloPolicyRepository extends JpaRepository<SloPolicy,String> {
}
