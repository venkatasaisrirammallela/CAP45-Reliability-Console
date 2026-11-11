package com.cap45.backend.controller;

import com.cap45.backend.dto.SimulationRequest;
import com.cap45.backend.entity.SimulationRun;
import com.cap45.backend.repository.SimulationRunRepository;
import com.cap45.backend.service.SimulationService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
@RestController @RequestMapping("/api/v1/simulations")
public class SimulationController {
 private final SimulationService simulator;private final SimulationRunRepository runs;
 public SimulationController(SimulationService s,SimulationRunRepository r){simulator=s;runs=r;}
 @PostMapping @ResponseStatus(HttpStatus.CREATED) public SimulationRun run(@Valid @RequestBody SimulationRequest request){return simulator.simulate(request);}
 @GetMapping("/{id}") public SimulationRun get(@PathVariable String id){return runs.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Run not found"));}
 @GetMapping public List<SimulationRun> list(@RequestParam String serviceId){return runs.findByServiceIdOrderByStartedAtDesc(serviceId);}
}
