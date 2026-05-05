package com.tuckersoft.tropelcare.signal.dto;

import com.tuckersoft.tropelcare.signal.TropelSignal;

import java.time.Instant;

public record SignalResponse(
        Long id,
        Long tropelId,
        String tropelName,
        Long guardianId,
        String guardianName,
        String senderTag,
        String rawContent,
        String signalType,
        String severity,
        String assignedUnit,
        String recommendedAction,
        String status,
        Instant createdAt,
        Instant updatedAt
) {
    public static SignalResponse from(TropelSignal s) {
        return new SignalResponse(
                s.getId(),
                s.getTropel().getId(), s.getTropel().getName(),
                s.getGuardian().getId(), s.getGuardian().getDisplayName(),
                s.getSenderTag(), s.getRawContent(),
                s.getSignalType(), s.getSeverity(), s.getAssignedUnit(), s.getRecommendedAction(),
                s.getStatus(), s.getCreatedAt(), s.getUpdatedAt()
        );
    }
}
