package com.cap45.backend.entity;

import jakarta.persistence.*;
import java.time.Instant;
@Entity @Table(name="simulation_runs")
public class SimulationRun {
 @Id public String id;
 @Column(nullable=false) public String serviceId;
 public String scenario;
 public int requestCount;
 public int failures;
 public Instant startedAt;
 public Instant completedAt;
 public String status;
 public SimulationRun() {}
}
