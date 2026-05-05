package com.tuckersoft.tropelcare.guardian.dto;

import com.tuckersoft.tropelcare.guardian.Guardian;

import java.time.Instant;

public record GuardianResponse(
        Long id,
        String displayName,
        String email,
        String notificationEmail,
        Instant createdAt
) {
    public static GuardianResponse from(Guardian g) {
        return new GuardianResponse(g.getId(), g.getDisplayName(), g.getEmail(),
                g.getNotificationEmail(), g.getCreatedAt());
    }
}
