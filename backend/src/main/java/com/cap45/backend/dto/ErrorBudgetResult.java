package com.cap45.backend.dto;

public record ErrorBudgetResult(String serviceId,long totalRequests,double availabilityTarget,double allowedFailures,double actualFailures,double remainingFailures,double consumedPercent,boolean exhausted) {}
