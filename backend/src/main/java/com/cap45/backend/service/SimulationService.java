package com.cap45.backend.service;

import com.cap45.backend.dto.SimulationRequest;
import com.cap45.backend.entity.*;
import com.cap45.backend.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.util.*;
@Service
public class SimulationService {
 private final ReliabilityService reliability;
 private final RequestEventRepository events;
 private final SimulationRunRepository runs;
 private final TelemetryRunRepository telemetry;
 public SimulationService(ReliabilityService r,RequestEventRepository e,SimulationRunRepository sr,TelemetryRunRepository t){reliability=r;events=e;runs=sr;telemetry=t;}
 @Transactional
 public SimulationRun simulate(SimulationRequest req){
  reliability.requireService(req.serviceId());String scenario=req.scenario().toUpperCase(Locale.ROOT);
  if(!Set.of("HEALTHY","LATENCY_DEGRADATION","ERROR_BURST","RECOVERY").contains(scenario))throw new IllegalArgumentException("scenario must be HEALTHY, LATENCY_DEGRADATION, ERROR_BURST or RECOVERY");
  Random random=req.seed()==null?new Random():new Random(req.seed());
  SimulationRun run=new SimulationRun();run.id=UUID.randomUUID().toString();run.serviceId=req.serviceId();run.scenario=scenario;run.requestCount=req.requestCount();run.startedAt=Instant.now();run.status="RUNNING";runs.save(run);
  List<RequestEvent> batch=new ArrayList<>();int failures=0;Instant now=Instant.now();
  for(int i=0;i<req.requestCount();i++){
   double failRate=switch(scenario){case "HEALTHY","RECOVERY" -> Math.min(req.failureRate(),0.001);case "ERROR_BURST" -> req.failureRate();default -> req.failureRate();};
   boolean failed=random.nextDouble()<failRate;if(failed)failures++;
   long latency=req.baseLatencyMs()+random.nextInt(101);
   if(scenario.equals("LATENCY_DEGRADATION"))latency+=500+random.nextInt(800);
   if(scenario.equals("RECOVERY"))latency=Math.max(1,latency/2);
   Instant when=now.minusMillis((long)(req.requestCount()-1-i)*req.durationSeconds()*1000L/Math.max(1,req.requestCount()));
   batch.add(new RequestEvent(req.serviceId(),when,failed?500:200,latency,run.id));
  }
  events.saveAll(batch);run.failures=failures;run.completedAt=Instant.now();run.status="COMPLETED";runs.save(run);
  TelemetryRun t=new TelemetryRun();t.serviceId=req.serviceId();t.status="SUCCESS";t.eventCount=batch.size();telemetry.save(t);return run;
 }
}
