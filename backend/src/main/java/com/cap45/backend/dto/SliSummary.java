package com.cap45.backend.dto;

public record SliSummary(String serviceId,int windowDays,long totalRequests,long successfulRequests,long fastRequests,double availabilityPercent,double latencyCompliancePercent,double availabilityTarget,double latencyTarget,boolean availabilityMet,boolean latencyMet,boolean hasTelemetry) {}
