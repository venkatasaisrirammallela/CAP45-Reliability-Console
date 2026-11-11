package com.cap45.backend.entity;

import jakarta.persistence.*;
import java.time.Instant;
@Entity @Table(name="telemetry_runs")
public class TelemetryRun {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 public String serviceId;
 public String source="SIMULATOR";
 public String status;
 public Instant observedAt=Instant.now();
 public long eventCount;
 public TelemetryRun() {}
}
