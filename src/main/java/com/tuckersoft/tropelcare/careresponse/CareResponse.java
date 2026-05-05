package com.tuckersoft.tropelcare.careresponse;

import com.tuckersoft.tropelcare.signal.TropelSignal;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "care_responses",
        uniqueConstraints = @UniqueConstraint(columnNames = "signal_id"))
public class CareResponse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "signal_id", nullable = false, unique = true)
    private TropelSignal signal;

    @Column(nullable = false)
    private String responseCode;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Instant createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public TropelSignal getSignal() { return signal; }
    public void setSignal(TropelSignal signal) { this.signal = signal; }

    public String getResponseCode() { return responseCode; }
    public void setResponseCode(String responseCode) { this.responseCode = responseCode; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
