package com.cap45.backend.entity;

import jakarta.persistence.*;
import java.time.Instant;
@Entity @Table(name="request_events",indexes={@Index(name="idx_event_service_time",columnList="serviceId,occurredAt")})
public class RequestEvent {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 @Column(nullable=false) public String serviceId;
 @Column(nullable=false) public Instant occurredAt;
 @Column(nullable=false) public int statusCode;
 @Column(nullable=false) public long latencyMs;
 public String simulationRunId;
 public RequestEvent() {}
 public RequestEvent(String s,Instant t,int c,long l,String run){serviceId=s;occurredAt=t;statusCode=c;latencyMs=l;simulationRunId=run;}
}
