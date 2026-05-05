package com.tuckersoft.tropelcare.sector;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "sectors", uniqueConstraints = @UniqueConstraint(columnNames = "sectorCode"))
public class Sector {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String sectorCode;

    @Column(nullable = false)
    private String climate;

    @Column(nullable = false)
    private Integer capacity;

    @Column(nullable = false)
    private Integer currentLoad;

    @Column(nullable = false)
    private Integer stabilityLevel;

    @Column(nullable = false)
    private Instant createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSectorCode() { return sectorCode; }
    public void setSectorCode(String sectorCode) { this.sectorCode = sectorCode; }

    public String getClimate() { return climate; }
    public void setClimate(String climate) { this.climate = climate; }

    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }

    public Integer getCurrentLoad() { return currentLoad; }
    public void setCurrentLoad(Integer currentLoad) { this.currentLoad = currentLoad; }

    public Integer getStabilityLevel() { return stabilityLevel; }
    public void setStabilityLevel(Integer stabilityLevel) { this.stabilityLevel = stabilityLevel; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
