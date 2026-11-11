package com.cap45.backend.dto;

import jakarta.validation.constraints.NotBlank;
public record ReleaseGateRequest(@NotBlank String serviceId,@NotBlank String version) {}
