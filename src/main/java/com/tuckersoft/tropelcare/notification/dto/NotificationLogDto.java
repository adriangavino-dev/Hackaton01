package com.tuckersoft.tropelcare.notification.dto;

import com.tuckersoft.tropelcare.notification.NotificationLog;

import java.time.Instant;

public record NotificationLogDto(
        Long id,
        Long signalId,
        String recipientEmail,
        String subject,
        String notifStatus,
        String errorMessage,
        Instant sentAt,
        Instant createdAt
) {
    public static NotificationLogDto from(NotificationLog n) {
        return new NotificationLogDto(n.getId(), n.getSignal().getId(),
                n.getRecipientEmail(), n.getSubject(), n.getNotifStatus(),
                n.getErrorMessage(), n.getSentAt(), n.getCreatedAt());
    }
}
