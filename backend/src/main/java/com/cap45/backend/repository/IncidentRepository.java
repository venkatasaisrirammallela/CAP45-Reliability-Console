package com.cap45.backend.repository;

import com.cap45.backend.entity.Incident;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.time.Instant;
public interface IncidentRepository extends JpaRepository<Incident,Long> {
 List<Incident> findByServiceIdOrderByOpenedAtDesc(String serviceId);
 long countByServiceIdAndStatus(String serviceId,String status);
}
