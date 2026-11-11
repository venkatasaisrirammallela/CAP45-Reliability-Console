package com.cap45.backend.service;

import com.cap45.backend.dto.*;
import com.cap45.backend.entity.*;
import com.cap45.backend.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.time.*;
import java.util.*;
@Service
public class ReliabilityService {
 private final ServiceRepository services;
 private final SloPolicyRepository policies;
 private final RequestEventRepository events;
 public ReliabilityService(ServiceRepository s,SloPolicyRepository p,RequestEventRepository e){services=s;policies=p;events=e;}
 public void requireService(String id){if(!services.existsById(id))throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Unknown service: "+id);}
 public SloPolicy policy(String id){requireService(id);return policies.findById(id).orElseGet(()->{SloPolicy p=new SloPolicy();p.serviceId=id;return p;});}
 public List<RequestEvent> window(String id,int days){requireService(id);return events.findByServiceIdAndOccurredAtGreaterThanEqualOrderByOccurredAtAsc(id,Instant.now().minus(Duration.ofDays(days)));}
 public SliSummary summary(String id){SloPolicy p=policy(id);List<RequestEvent> rows=window(id,p.windowDays);long n=rows.size();long ok=rows.stream().filter(e->e.statusCode>=200&&e.statusCode<500).count();long fast=rows.stream().filter(e->e.statusCode>=200&&e.statusCode<500&&e.latencyMs<=p.latencyThresholdMs).count();double a=n==0?0:100.0*ok/n;double l=ok==0?0:100.0*fast/ok;return new SliSummary(id,p.windowDays,n,ok,fast,a,l,p.availabilityTarget,p.latencyTarget,n>0&&a>=p.availabilityTarget,ok>0&&l>=p.latencyTarget,n>0);}
 public ErrorBudgetResult budget(String id){SliSummary s=summary(id);double allowed=s.totalRequests()*(1-s.availabilityTarget()/100.0);double actual=s.totalRequests()-s.successfulRequests();double remaining=allowed-actual;double consumed=allowed>0?100.0*actual/allowed:0;return new ErrorBudgetResult(id,s.totalRequests(),s.availabilityTarget(),allowed,actual,remaining,consumed,actual>allowed);}
 public BurnRateResult burn(String id,int hours){if(hours<1||hours>720)throw new IllegalArgumentException("hours must be between 1 and 720");SloPolicy p=policy(id);List<RequestEvent> rows=events.findByServiceIdAndOccurredAtGreaterThanEqualOrderByOccurredAtAsc(id,Instant.now().minus(Duration.ofHours(hours)));long n=rows.size();long bad=rows.stream().filter(e->e.statusCode<200||e.statusCode>=500).count();double error=n==0?0:(double)bad/n;double allowed=1-p.availabilityTarget/100.0;return new BurnRateResult(id,hours,n,bad,error,allowed,n==0||allowed<=0?0:error/allowed,n>0);}
 public List<Map<String,Object>> trend(String id,int days){List<RequestEvent> rows=window(id,days);Map<LocalDate,List<RequestEvent>> byDay=new TreeMap<>();for(RequestEvent e:rows)byDay.computeIfAbsent(e.occurredAt.atZone(ZoneOffset.UTC).toLocalDate(),k->new ArrayList<>()).add(e);List<Map<String,Object>> result=new ArrayList<>();for(var item:byDay.entrySet()){long n=item.getValue().size(),ok=item.getValue().stream().filter(e->e.statusCode>=200&&e.statusCode<500).count();result.add(Map.of("date",item.getKey().toString(),"totalRequests",n,"successfulRequests",ok,"availabilityPercent",100.0*ok/n));}return result;}
}
