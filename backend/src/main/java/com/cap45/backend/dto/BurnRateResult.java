package com.cap45.backend.dto;

public record BurnRateResult(String serviceId,int hours,long totalRequests,long failures,double observedErrorRate,double allowedErrorRate,double burnRate,boolean hasTelemetry) {}
