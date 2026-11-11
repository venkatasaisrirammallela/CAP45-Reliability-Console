package com.cap45.backend.entity;

import jakarta.persistence.*;
import java.time.Instant;
@Entity @Table(name="incidents")
public class Incident {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 @Column(nullable=false) public String serviceId;
 @Column(nullable=false) public String title;
 public String description;
 public String severity="SEV3";
 public String status="OPEN";
 public Instant openedAt=Instant.now();
 public Instant resolvedAt;
 public Incident() {}
}
