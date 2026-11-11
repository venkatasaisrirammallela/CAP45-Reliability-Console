package com.cap45.backend.dto;

import jakarta.validation.constraints.*;
public record SimulationRequest(
 @NotBlank String serviceId,
 @NotBlank String scenario,
 @Min(1) @Max(10000) int requestCount,
 @DecimalMin("0.0") @DecimalMax("1.0") double failureRate,
 @Min(1) long baseLatencyMs,
 @Min(1) @Max(86400) int durationSeconds,
 Long seed
) {}
