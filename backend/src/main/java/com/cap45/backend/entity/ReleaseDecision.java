package com.cap45.backend.entity;

import jakarta.persistence.*;
import java.time.Instant;
@Entity @Table(name="release_decisions")
public class ReleaseDecision {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 public String serviceId;
 public String version;
 public String decision;
 @Column(length=2000) public String reason;
 public Instant evaluatedAt=Instant.now();
 public ReleaseDecision() {}
}
