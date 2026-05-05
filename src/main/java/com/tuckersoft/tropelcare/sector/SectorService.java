package com.tuckersoft.tropelcare.sector;

import com.tuckersoft.tropelcare.common.ApiException;
import com.tuckersoft.tropelcare.sector.dto.SectorRequest;
import com.tuckersoft.tropelcare.sector.dto.SectorResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Set;

@Service
@Transactional(readOnly = true)
public class SectorService {

    private static final Set<String> CLIMATES = Set.of(
            "PIXEL_FOREST", "NEON_CAVE", "CLOUD_AQUARIUM", "RETRO_ARCADE");

    private final SectorRepository sectors;

    public SectorService(SectorRepository sectors) {
        this.sectors = sectors;
    }

    @Transactional
    public SectorResponse create(SectorRequest req) {
        if (!CLIMATES.contains(req.climate())) {
            throw ApiException.badRequest("Clima invalido. Valores: " + CLIMATES);
        }
        if (sectors.existsBySectorCode(req.sectorCode())) {
            throw ApiException.conflict("Ya existe un sector con codigo " + req.sectorCode());
        }
        Sector s = new Sector();
        s.setSectorCode(req.sectorCode());
        s.setClimate(req.climate());
        s.setCapacity(req.capacity());
        s.setCurrentLoad(0);
        s.setStabilityLevel(100);
        s.setCreatedAt(Instant.now());
        return SectorResponse.from(sectors.save(s));
    }

    public List<SectorResponse> findAll() {
        return sectors.findAll().stream().map(SectorResponse::from).toList();
    }

    public SectorResponse findById(Long id) {
        return SectorResponse.from(getOrThrow(id));
    }

    public Sector getOrThrow(Long id) {
        return sectors.findById(id)
                .orElseThrow(() -> ApiException.notFound("No existe un sector con id " + id));
    }
}
