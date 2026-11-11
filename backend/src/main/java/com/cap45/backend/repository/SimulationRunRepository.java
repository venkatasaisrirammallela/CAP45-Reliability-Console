package com.cap45.backend.repository;

import com.cap45.backend.entity.SimulationRun;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.time.Instant;
public interface SimulationRunRepository extends JpaRepository<SimulationRun,String> {
 List<SimulationRun> findByServiceIdOrderByStartedAtDesc(String serviceId);
}
