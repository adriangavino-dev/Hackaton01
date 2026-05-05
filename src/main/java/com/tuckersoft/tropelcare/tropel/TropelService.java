package com.tuckersoft.tropelcare.tropel;

import com.tuckersoft.tropelcare.common.ApiException;
import com.tuckersoft.tropelcare.common.PageResponse;
import com.tuckersoft.tropelcare.guardian.Guardian;
import com.tuckersoft.tropelcare.guardian.GuardianRepository;
import com.tuckersoft.tropelcare.sector.Sector;
import com.tuckersoft.tropelcare.sector.SectorRepository;
import com.tuckersoft.tropelcare.signal.TropelSignal;
import com.tuckersoft.tropelcare.signal.TropelSignalRepository;
import com.tuckersoft.tropelcare.tropel.dto.DiaryResponse;
import com.tuckersoft.tropelcare.tropel.dto.TropelCreateRequest;
import com.tuckersoft.tropelcare.tropel.dto.TropelResponse;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@Transactional(readOnly = true)
public class TropelService {

    private static final Set<String> SPECIES = Set.of(
            "BLOBITO", "CHISPA", "GRUNON", "GRUÑON", "DORMILON", "GLITCHY");

    private final TropelRepository tropels;
    private final SectorRepository sectors;
    private final GuardianRepository guardians;
    private final TropelSignalRepository signals;

    public TropelService(TropelRepository tropels, SectorRepository sectors,
                         GuardianRepository guardians, TropelSignalRepository signals) {
        this.tropels = tropels;
        this.sectors = sectors;
        this.guardians = guardians;
        this.signals = signals;
    }

    @Transactional
    public TropelResponse create(TropelCreateRequest req) {
        if (!SPECIES.contains(req.species())) {
            throw ApiException.badRequest("Especie invalida. Valores: BLOBITO, CHISPA, GRUNON, DORMILON, GLITCHY");
        }
        if (tropels.existsByName(req.name())) {
            throw ApiException.conflict("Ya existe un Tropel con nombre " + req.name());
        }
        Sector sector = sectors.findById(req.sectorId())
                .orElseThrow(() -> ApiException.notFound("No existe un sector con id " + req.sectorId()));
        Guardian guardian = guardians.findById(req.guardianId())
                .orElseThrow(() -> ApiException.notFound("No existe un guardian con id " + req.guardianId()));
        if (sector.getCurrentLoad() >= sector.getCapacity()) {
            throw ApiException.badRequest("El sector " + sector.getSectorCode() + " esta lleno");
        }

        Tropel t = new Tropel();
        t.setName(req.name());
        t.setSpecies(req.species());
        t.setVitalState("ESTABLE");
        t.setEnergyLevel(80);
        t.setChaosIndex(10);
        t.setMutationStage(0);
        t.setSector(sector);
        t.setGuardian(guardian);
        Instant now = Instant.now();
        t.setCreatedAt(now);
        t.setUpdatedAt(now);

        sector.setCurrentLoad(sector.getCurrentLoad() + 1);
        sectors.save(sector);

        return TropelResponse.from(tropels.save(t));
    }

    public PageResponse<TropelResponse> search(String species, String vitalState,
                                               Long sectorId, Long guardianId,
                                               int page, int size) {
        Specification<Tropel> spec = (root, q, cb) -> {
            List<Predicate> ps = new ArrayList<>();
            if (species != null) ps.add(cb.equal(root.get("species"), species));
            if (vitalState != null) ps.add(cb.equal(root.get("vitalState"), vitalState));
            if (sectorId != null) ps.add(cb.equal(root.get("sector").get("id"), sectorId));
            if (guardianId != null) ps.add(cb.equal(root.get("guardian").get("id"), guardianId));
            return cb.and(ps.toArray(new Predicate[0]));
        };
        Page<Tropel> result = tropels.findAll(spec,
                PageRequest.of(page, size, Sort.by("id").ascending()));
        return PageResponse.from(result, TropelResponse::from);
    }

    public TropelResponse findById(Long id) {
        return TropelResponse.from(getOrThrow(id));
    }

    public Tropel getOrThrow(Long id) {
        return tropels.findById(id)
                .orElseThrow(() -> ApiException.notFound("No existe un Tropel con id " + id));
    }

    public DiaryResponse diary(Long tropelId) {
        Tropel t = getOrThrow(tropelId);
        List<DiaryResponse.Note> notes = signals
                .findByTropelIdAndPersonalityNoteIsNotNullOrderByCreatedAtDesc(tropelId)
                .stream()
                .map(s -> new DiaryResponse.Note(s.getId(), s.getPersonalityNote(), s.getCreatedAt()))
                .toList();
        return new DiaryResponse(t.getId(), t.getName(), notes);
    }
}
