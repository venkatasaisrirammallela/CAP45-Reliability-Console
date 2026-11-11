package com.cap45.backend.controller;

import com.cap45.backend.entity.SloPolicy;
import com.cap45.backend.repository.SloPolicyRepository;
import com.cap45.backend.service.ReliabilityService;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/v1/slo-policies")
public class SloPolicyController {
 private final SloPolicyRepository repo;private final ReliabilityService reliability;
 public SloPolicyController(SloPolicyRepository r,ReliabilityService s){repo=r;reliability=s;}
 @GetMapping("/{serviceId}") public SloPolicy get(@PathVariable String serviceId){return reliability.policy(serviceId);}
 @PutMapping("/{serviceId}") public SloPolicy put(@PathVariable String serviceId,@RequestBody SloPolicy p){reliability.requireService(serviceId);if(p.availabilityTarget<=0||p.availabilityTarget>=100||p.latencyTarget<=0||p.latencyTarget>100||p.latencyThresholdMs<=0||p.windowDays<1||p.windowDays>365)throw new IllegalArgumentException("Invalid SLO targets, threshold or window");p.serviceId=serviceId;return repo.save(p);}
}
