package com.cap45.backend.repository;

import com.cap45.backend.entity.RequestEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.time.Instant;
public interface RequestEventRepository extends JpaRepository<RequestEvent,Long> {
 List<RequestEvent> findByServiceIdAndOccurredAtGreaterThanEqualOrderByOccurredAtAsc(String serviceId,Instant from);
 long countByServiceId(String serviceId);
}
