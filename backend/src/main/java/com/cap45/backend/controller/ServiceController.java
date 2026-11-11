package com.cap45.backend.controller;

import com.cap45.backend.entity.ServiceEntity;
import com.cap45.backend.repository.ServiceRepository;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
@RestController @RequestMapping("/api/v1/services")
public class ServiceController {
 private final ServiceRepository repo;
 public ServiceController(ServiceRepository r){repo=r;}
 @GetMapping public List<ServiceEntity> all(){return repo.findAll();}
 @GetMapping("/{id}") public ServiceEntity one(@PathVariable String id){return repo.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Service not found"));}
 @PostMapping @ResponseStatus(HttpStatus.CREATED) public ServiceEntity create(@RequestBody ServiceEntity s){if(s.serviceId==null||s.serviceId.isBlank()||s.serviceName==null||s.serviceName.isBlank())throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"serviceId and serviceName required");if(repo.existsById(s.serviceId))throw new ResponseStatusException(HttpStatus.CONFLICT,"Service exists");return repo.save(s);}
}
