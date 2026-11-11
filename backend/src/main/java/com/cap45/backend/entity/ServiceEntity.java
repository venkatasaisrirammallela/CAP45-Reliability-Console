package com.cap45.backend.entity;

import jakarta.persistence.*;
@Entity @Table(name="services")
public class ServiceEntity {
 @Id @Column(name="service_id",length=100) public String serviceId;
 @Column(name="service_name",nullable=false) public String serviceName;
 public String description;
 public ServiceEntity() {}
 public String getServiceId(){return serviceId;} public void setServiceId(String v){serviceId=v;}
 public String getServiceName(){return serviceName;} public void setServiceName(String v){serviceName=v;}
 public String getDescription(){return description;} public void setDescription(String v){description=v;}
}
