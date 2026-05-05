package com.tuckersoft.tropelcare.sector.dto;

import com.tuckersoft.tropelcare.sector.Sector;

import java.time.Instant;

public record SectorResponse(
        Long id,
        String sectorCode,
        String climate,
        Integer capacity,
        Integer currentLoad,
        Integer stabilityLevel,
        Instant createdAt
) {
    public static SectorResponse from(Sector s) {
        return new SectorResponse(s.getId(), s.getSectorCode(), s.getClimate(),
                s.getCapacity(), s.getCurrentLoad(), s.getStabilityLevel(), s.getCreatedAt());
    }
}
