package com.cap45.backend.repository;

import com.cap45.backend.entity.ServiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.time.Instant;
public interface ServiceRepository extends JpaRepository<ServiceEntity,String> {
}
