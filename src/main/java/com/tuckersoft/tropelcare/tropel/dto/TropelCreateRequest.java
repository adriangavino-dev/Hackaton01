package com.tuckersoft.tropelcare.tropel.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TropelCreateRequest(
        @NotBlank @Size(min = 2, max = 40) String name,
        @NotBlank String species,
        @NotNull Long sectorId,
        @NotNull Long guardianId
) {}
