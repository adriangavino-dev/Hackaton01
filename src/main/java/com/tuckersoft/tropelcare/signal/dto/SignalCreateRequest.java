package com.tuckersoft.tropelcare.signal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SignalCreateRequest(
        @NotNull Long tropelId,
        @NotNull Long guardianId,
        @NotBlank String senderTag,
        @NotBlank @Size(min = 10) String rawContent
) {}
