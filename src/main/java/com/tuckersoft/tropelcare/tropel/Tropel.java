package com.tuckersoft.tropelcare.tropel;

import com.tuckersoft.tropelcare.guardian.Guardian;
import com.tuckersoft.tropelcare.sector.Sector;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "tropels", uniqueConstraints = @UniqueConstraint(columnNames = "name"))
public class Tropel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 40)
    private String name;

    @Column(nullable = false)
    private String species;

    @Column(nullable = false)
    private String vitalState;

    @Column(nullable = false)
    private Integer energyLevel;

    @Column(nullable = false)
    private Integer chaosIndex;

    @Column(nullable = false)
    private Integer mutationStage;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sector_id", nullable = false)
    private Sector sector;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "guardian_id", nullable = false)
    private Guardian guardian;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSpecies() { return species; }
    public void setSpecies(String species) { this.species = species; }

    public String getVitalState() { return vitalState; }
    public void setVitalState(String vitalState) { this.vitalState = vitalState; }

    public Integer getEnergyLevel() { return energyLevel; }
    public void setEnergyLevel(Integer energyLevel) { this.energyLevel = energyLevel; }

    public Integer getChaosIndex() { return chaosIndex; }
    public void setChaosIndex(Integer chaosIndex) { this.chaosIndex = chaosIndex; }

    public Integer getMutationStage() { return mutationStage; }
    public void setMutationStage(Integer mutationStage) { this.mutationStage = mutationStage; }

    public Sector getSector() { return sector; }
    public void setSector(Sector sector) { this.sector = sector; }

    public Guardian getGuardian() { return guardian; }
    public void setGuardian(Guardian guardian) { this.guardian = guardian; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
