package com.tuckersoft.tropelcare.sector.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record SectorRequest(
        @NotBlank String sectorCode,
        @NotBlank String climate,
        @NotNull @Positive Integer capacity
) {}
