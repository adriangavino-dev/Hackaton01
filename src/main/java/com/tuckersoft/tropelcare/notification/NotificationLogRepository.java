package com.tuckersoft.tropelcare.notification;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificationLogRepository extends JpaRepository<NotificationLog, Long> {
    List<NotificationLog> findBySignalIdOrderByCreatedAtAsc(Long signalId);
}
