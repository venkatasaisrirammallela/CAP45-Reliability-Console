package com.cap45.backend.entity;

import jakarta.persistence.*;
@Entity @Table(name="slo_policies")
public class SloPolicy {
 @Id @Column(name="service_id") public String serviceId;
 @Column(nullable=false) public double availabilityTarget=99.9;
 @Column(nullable=false) public double latencyTarget=95.0;
 @Column(nullable=false) public long latencyThresholdMs=500;
 @Column(nullable=false) public int windowDays=30;
 public SloPolicy() {}
}
