package com.tuckersoft.tropelcare.sector;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SectorRepository extends JpaRepository<Sector, Long> {
    boolean existsBySectorCode(String sectorCode);
}
