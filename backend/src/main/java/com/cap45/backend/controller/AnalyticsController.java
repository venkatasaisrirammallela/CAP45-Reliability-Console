package com.cap45.backend.controller;

import com.cap45.backend.dto.*;
import com.cap45.backend.service.ReliabilityService;
import org.springframework.web.bind.annotation.*;
import java.util.*;
@RestController @RequestMapping("/api/v1")
public class AnalyticsController {
 private final ReliabilityService reliability;
 public AnalyticsController(ReliabilityService r){reliability=r;}
 @GetMapping("/sli/summary") public SliSummary summary(@RequestParam String serviceId){return reliability.summary(serviceId);}
 @GetMapping("/sli/trend") public List<Map<String,Object>> trend(@RequestParam String serviceId,@RequestParam(defaultValue="30") int days){if(days<1||days>365)throw new IllegalArgumentException("days must be 1..365");return reliability.trend(serviceId,days);}
 @GetMapping("/error-budget") public ErrorBudgetResult budget(@RequestParam String serviceId){return reliability.budget(serviceId);}
 @GetMapping("/burn-rate") public BurnRateResult burn(@RequestParam String serviceId,@RequestParam(defaultValue="1") int hours){return reliability.burn(serviceId,hours);}
}
