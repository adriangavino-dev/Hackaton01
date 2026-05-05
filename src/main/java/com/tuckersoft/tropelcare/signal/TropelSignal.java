package com.tuckersoft.tropelcare.signal;

import com.tuckersoft.tropelcare.guardian.Guardian;
import com.tuckersoft.tropelcare.tropel.Tropel;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "tropel_signals")
public class TropelSignal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tropel_id", nullable = false)
    private Tropel tropel;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "guardian_id", nullable = false)
    private Guardian guardian;

    @Column(nullable = false)
    private String senderTag;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String rawContent;

    @Column(nullable = false)
    private String signalType;

    @Column(nullable = false)
    private String severity;

    @Column(nullable = false)
    private String assignedUnit;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String recommendedAction;

    @Column(nullable = false)
    private String status;

    @Column(columnDefinition = "TEXT")
    private String personalityNote;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Tropel getTropel() { return tropel; }
    public void setTropel(Tropel tropel) { this.tropel = tropel; }

    public Guardian getGuardian() { return guardian; }
    public void setGuardian(Guardian guardian) { this.guardian = guardian; }

    public String getSenderTag() { return senderTag; }
    public void setSenderTag(String senderTag) { this.senderTag = senderTag; }

    public String getRawContent() { return rawContent; }
    public void setRawContent(String rawContent) { this.rawContent = rawContent; }

    public String getSignalType() { return signalType; }
    public void setSignalType(String signalType) { this.signalType = signalType; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public String getAssignedUnit() { return assignedUnit; }
    public void setAssignedUnit(String assignedUnit) { this.assignedUnit = assignedUnit; }

    public String getRecommendedAction() { return recommendedAction; }
    public void setRecommendedAction(String recommendedAction) { this.recommendedAction = recommendedAction; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPersonalityNote() { return personalityNote; }
    public void setPersonalityNote(String personalityNote) { this.personalityNote = personalityNote; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
