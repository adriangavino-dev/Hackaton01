package com.tuckersoft.tropelcare.tropel.dto;

import com.tuckersoft.tropelcare.tropel.Tropel;

import java.time.Instant;

public record TropelResponse(
        Long id,
        String name,
        String species,
        String vitalState,
        Integer energyLevel,
        Integer chaosIndex,
        Integer mutationStage,
        Long sectorId,
        String sectorCode,
        Long guardianId,
        String guardianName,
        Instant createdAt,
        Instant updatedAt
) {
    public static TropelResponse from(Tropel t) {
        return new TropelResponse(
                t.getId(), t.getName(), t.getSpecies(), t.getVitalState(),
                t.getEnergyLevel(), t.getChaosIndex(), t.getMutationStage(),
                t.getSector().getId(), t.getSector().getSectorCode(),
                t.getGuardian().getId(), t.getGuardian().getDisplayName(),
                t.getCreatedAt(), t.getUpdatedAt()
        );
    }
}
