package com.cap45.backend.controller;

import com.cap45.backend.entity.TelemetryRun;
import com.cap45.backend.repository.TelemetryRunRepository;
import com.cap45.backend.service.ReliabilityService;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController @RequestMapping("/api/v1/telemetry")
public class TelemetryController {
 private final TelemetryRunRepository repo;private final ReliabilityService reliability;
 public TelemetryController(TelemetryRunRepository r,ReliabilityService s){repo=r;reliability=s;}
 @GetMapping("/runs") public List<TelemetryRun> runs(@RequestParam String serviceId){reliability.requireService(serviceId);return repo.findByServiceIdOrderByObservedAtDesc(serviceId);}
}
