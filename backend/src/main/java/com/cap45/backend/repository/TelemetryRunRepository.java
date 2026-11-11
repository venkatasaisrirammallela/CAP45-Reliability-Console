package com.cap45.backend.repository;

import com.cap45.backend.entity.TelemetryRun;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.time.Instant;
public interface TelemetryRunRepository extends JpaRepository<TelemetryRun,Long> {
 List<TelemetryRun> findByServiceIdOrderByObservedAtDesc(String serviceId);
}
