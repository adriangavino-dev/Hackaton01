package com.tuckersoft.tropelcare.careresponse.dto;

import com.tuckersoft.tropelcare.careresponse.CareResponse;

import java.time.Instant;

public record CareResponseDto(
        Long id,
        Long signalId,
        String responseCode,
        String description,
        Instant createdAt
) {
    public static CareResponseDto from(CareResponse cr) {
        return new CareResponseDto(cr.getId(), cr.getSignal().getId(),
                cr.getResponseCode(), cr.getDescription(), cr.getCreatedAt());
    }
}
