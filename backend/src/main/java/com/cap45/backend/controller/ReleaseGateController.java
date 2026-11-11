package com.cap45.backend.controller;

import com.cap45.backend.dto.*;
import com.cap45.backend.entity.*;
import com.cap45.backend.repository.*;
import com.cap45.backend.service.ReliabilityService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.util.*;
@RestController @RequestMapping("/api/v1/release-gate")
public class ReleaseGateController {
 private final ReliabilityService reliability;private final IncidentRepository incidents;private final ReleaseDecisionRepository decisions;
 public ReleaseGateController(ReliabilityService r,IncidentRepository i,ReleaseDecisionRepository d){reliability=r;incidents=i;decisions=d;}
 @PostMapping public ReleaseDecision evaluate(@Valid @RequestBody ReleaseGateRequest request){
  String id=request.serviceId();SliSummary s=reliability.summary(id);ErrorBudgetResult b=reliability.budget(id);BurnRateResult burn=reliability.burn(id,1);
  ReleaseDecision d=new ReleaseDecision();d.serviceId=id;d.version=request.version();
  if(!s.hasTelemetry()||!burn.hasTelemetry()){d.decision="BLOCK";d.reason="Missing telemetry: fail closed";}
  else if(incidents.countByServiceIdAndStatus(id,"OPEN")>0){d.decision="BLOCK";d.reason="Open incident exists";}
  else if(b.exhausted()||burn.burnRate()>=14.4){d.decision="BLOCK";d.reason="Error budget exhausted or critical burn rate";}
  else if(!s.availabilityMet()||!s.latencyMet()||burn.burnRate()>=2){d.decision="REQUIRE_REVIEW";d.reason="SLO target missed or elevated burn rate";}
  else{d.decision="ALLOW";d.reason="SLOs healthy and error budget available";}
  return decisions.save(d);
 }
 @GetMapping public List<ReleaseDecision> history(@RequestParam String serviceId){reliability.requireService(serviceId);return decisions.findByServiceIdOrderByEvaluatedAtDesc(serviceId);}
}
