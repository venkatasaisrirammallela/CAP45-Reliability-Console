package com.cap45.backend.repository;

import com.cap45.backend.entity.ReleaseDecision;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.time.Instant;
public interface ReleaseDecisionRepository extends JpaRepository<ReleaseDecision,Long> {
 List<ReleaseDecision> findByServiceIdOrderByEvaluatedAtDesc(String serviceId);
}
