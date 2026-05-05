package com.tuckersoft.tropelcare.notification;

import com.tuckersoft.tropelcare.signal.TropelSignal;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "notification_logs")
public class NotificationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "signal_id", nullable = false)
    private TropelSignal signal;

    @Column(nullable = false)
    private String recipientEmail;

    @Column(nullable = false)
    private String subject;

    @Column(nullable = false)
    private String notifStatus;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    private Instant sentAt;

    @Column(nullable = false)
    private Instant createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public TropelSignal getSignal() { return signal; }
    public void setSignal(TropelSignal signal) { this.signal = signal; }

    public String getRecipientEmail() { return recipientEmail; }
    public void setRecipientEmail(String recipientEmail) { this.recipientEmail = recipientEmail; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getNotifStatus() { return notifStatus; }
    public void setNotifStatus(String notifStatus) { this.notifStatus = notifStatus; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public Instant getSentAt() { return sentAt; }
    public void setSentAt(Instant sentAt) { this.sentAt = sentAt; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
