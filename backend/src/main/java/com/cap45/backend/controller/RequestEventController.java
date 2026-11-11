package com.cap45.backend.controller;

import com.cap45.backend.entity.RequestEvent;
import com.cap45.backend.repository.RequestEventRepository;
import com.cap45.backend.service.ReliabilityService;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController @RequestMapping("/api/v1/requests")
public class RequestEventController {
 private final RequestEventRepository repo;private final ReliabilityService reliability;
 public RequestEventController(RequestEventRepository r,ReliabilityService s){repo=r;reliability=s;}
 @GetMapping public List<RequestEvent> list(@RequestParam String serviceId,@RequestParam(defaultValue="30") int days){if(days<1||days>365)throw new IllegalArgumentException("days must be 1..365");return reliability.window(serviceId,days);}
}
