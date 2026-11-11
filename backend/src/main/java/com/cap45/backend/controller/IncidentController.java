package com.cap45.backend.controller;

import com.cap45.backend.entity.Incident;
import com.cap45.backend.repository.IncidentRepository;
import com.cap45.backend.service.ReliabilityService;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;
import org.springframework.web.server.ResponseStatusException;
import java.time.Instant;
import java.util.List;
@RestController @RequestMapping("/api/v1/incidents")
public class IncidentController {
 private final IncidentRepository repo;private final ReliabilityService reliability;
 public IncidentController(IncidentRepository r,ReliabilityService s){repo=r;reliability=s;}
 @GetMapping public List<Incident> all(@RequestParam String serviceId){reliability.requireService(serviceId);return repo.findByServiceIdOrderByOpenedAtDesc(serviceId);}
 @PostMapping @ResponseStatus(HttpStatus.CREATED) public Incident create(@RequestBody Incident i){reliability.requireService(i.serviceId);if(i.title==null||i.title.isBlank())throw new IllegalArgumentException("title required");i.id=null;i.status="OPEN";i.openedAt=Instant.now();i.resolvedAt=null;return repo.save(i);}
 @PatchMapping("/{id}/resolve") public Incident resolve(@PathVariable Long id){Incident i=repo.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Incident not found"));i.status="RESOLVED";i.resolvedAt=Instant.now();return repo.save(i);}
}
