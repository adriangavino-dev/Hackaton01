package com.tuckersoft.tropelcare.signal;

import com.tuckersoft.tropelcare.ai.ClassificationResult;
import com.tuckersoft.tropelcare.ai.SignalClassifier;
import com.tuckersoft.tropelcare.careresponse.CareResponse;
import com.tuckersoft.tropelcare.careresponse.CareResponseRepository;
import com.tuckersoft.tropelcare.careresponse.ResponseCodeMapper;
import com.tuckersoft.tropelcare.common.ApiException;
import com.tuckersoft.tropelcare.common.PageResponse;
import com.tuckersoft.tropelcare.guardian.Guardian;
import com.tuckersoft.tropelcare.guardian.GuardianRepository;
import com.tuckersoft.tropelcare.sector.Sector;
import com.tuckersoft.tropelcare.sector.SectorRepository;
import com.tuckersoft.tropelcare.signal.dto.SignalCreateRequest;
import com.tuckersoft.tropelcare.signal.dto.SignalResponse;
import com.tuckersoft.tropelcare.tropel.Tropel;
import com.tuckersoft.tropelcare.tropel.TropelRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class SignalService {

    private final TropelSignalRepository signals;
    private final TropelRepository tropels;
    private final GuardianRepository guardians;
    private final SectorRepository sectors;
    private final CareResponseRepository careResponses;
    private final SignalClassifier classifier;
    private final ApplicationEventPublisher events;

    public SignalService(TropelSignalRepository signals,
                         TropelRepository tropels,
                         GuardianRepository guardians,
                         SectorRepository sectors,
                         CareResponseRepository careResponses,
                         SignalClassifier classifier,
                         ApplicationEventPublisher events) {
        this.signals = signals;
        this.tropels = tropels;
        this.guardians = guardians;
        this.sectors = sectors;
        this.careResponses = careResponses;
        this.classifier = classifier;
        this.events = events;
    }

    @Transactional
    public SignalResponse create(SignalCreateRequest req) {
        Tropel tropel = tropels.findById(req.tropelId())
                .orElseThrow(() -> ApiException.notFound("No existe un Tropel con id " + req.tropelId()));
        Guardian guardian = guardians.findById(req.guardianId())
                .orElseThrow(() -> ApiException.notFound("No existe un guardian con id " + req.guardianId()));

        if (!tropel.getGuardian().getId().equals(req.guardianId())) {
            throw ApiException.badRequest(
                    "El guardianId no corresponde al guardian responsable de este Tropel");
        }

        ClassificationResult cls = classifier.classify(req.rawContent());
        Instant now = Instant.now();
        Sector sector = tropel.getSector();

        if (!cls.fallback()) {
            applyStatChanges(tropel, cls.severity());
            applyVitalState(tropel, cls.severity());
            tropel.setUpdatedAt(now);

            applySectorStability(sector, cls.signalType());

            tropels.save(tropel);
            sectors.save(sector);
        }

        TropelSignal signal = new TropelSignal();
        signal.setTropel(tropel);
        signal.setGuardian(guardian);
        signal.setSenderTag(req.senderTag());
        signal.setRawContent(req.rawContent());
        signal.setSignalType(cls.signalType());
        signal.setSeverity(cls.severity());
        signal.setAssignedUnit(cls.assignedUnit());
        signal.setRecommendedAction(cls.recommendedAction());
        signal.setPersonalityNote(cls.personalityNote());
        signal.setStatus(cls.fallback() ? "ERROR" : "RECIBIDA");
        signal.setCreatedAt(now);
        signal.setUpdatedAt(now);
        signal = signals.save(signal);

        CareResponse cr = new CareResponse();
        cr.setSignal(signal);
        cr.setResponseCode(ResponseCodeMapper.forSignalType(cls.signalType()));
        cr.setDescription(cls.recommendedAction());
        cr.setCreatedAt(now);
        careResponses.save(cr);

        if (!cls.fallback()) {
            events.publishEvent(new TropelSignalCreatedEvent(signal.getId()));
        }

        return SignalResponse.from(signal);
    }

    private void applyStatChanges(Tropel t, String severity) {
        int dEnergy, dChaos, dMutation;
        switch (severity) {
            case "LEVE" -> { dEnergy = -5;  dChaos = 5;  dMutation = 0; }
            case "MODERADO" -> { dEnergy = -10; dChaos = 15; dMutation = 0; }
            case "GRAVE" -> { dEnergy = -20; dChaos = 30; dMutation = 0; }
            case "CRITICO" -> { dEnergy = -30; dChaos = 45; dMutation = 1; }
            default -> { dEnergy = 0; dChaos = 0; dMutation = 0; }
        }
        t.setEnergyLevel(clamp(t.getEnergyLevel() + dEnergy, 0, 100));
        t.setChaosIndex(clamp(t.getChaosIndex() + dChaos, 0, 100));
        t.setMutationStage(clamp(t.getMutationStage() + dMutation, 0, 5));
    }

    private void applyVitalState(Tropel t, String severity) {
        if (t.getChaosIndex() >= 80) {
            t.setVitalState("CRITICO");
        } else if (t.getEnergyLevel() <= 20) {
            t.setVitalState("HAMBRIENTO");
        } else if ("CRITICO".equals(severity)) {
            t.setVitalState("MUTANDO");
        } else if ("GRAVE".equals(severity)) {
            t.setVitalState("AGITADO");
        }
    }

    private void applySectorStability(Sector s, String signalType) {
        int delta = switch (signalType) {
            case "FUGA" -> -10;
            case "REPRODUCCION_MASIVA" -> -15;
            default -> 0;
        };
        if (delta != 0) s.setStabilityLevel(Math.max(0, s.getStabilityLevel() + delta));
    }

    private static int clamp(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }

    public SignalResponse findById(Long id) {
        return SignalResponse.from(getOrThrow(id));
    }

    public TropelSignal getOrThrow(Long id) {
        return signals.findById(id)
                .orElseThrow(() -> ApiException.notFound("No existe una senal con id " + id));
    }

    public PageResponse<SignalResponse> search(String signalType, String severity, String status,
                                               Long tropelId, Long guardianId,
                                               @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                               @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
                                               int page, int size) {
        Specification<TropelSignal> spec = (root, q, cb) -> {
            List<Predicate> ps = new ArrayList<>();
            if (signalType != null) ps.add(cb.equal(root.get("signalType"), signalType));
            if (severity != null) ps.add(cb.equal(root.get("severity"), severity));
            if (status != null) ps.add(cb.equal(root.get("status"), status));
            if (tropelId != null) ps.add(cb.equal(root.get("tropel").get("id"), tropelId));
            if (guardianId != null) ps.add(cb.equal(root.get("guardian").get("id"), guardianId));
            if (from != null) ps.add(cb.greaterThanOrEqualTo(root.get("createdAt"),
                    from.atStartOfDay().toInstant(ZoneOffset.UTC)));
            if (to != null) ps.add(cb.lessThan(root.get("createdAt"),
                    to.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC)));
            return cb.and(ps.toArray(new Predicate[0]));
        };
        Page<TropelSignal> result = signals.findAll(spec,
                PageRequest.of(page, size, Sort.by("id").descending()));
        return PageResponse.from(result, SignalResponse::from);
    }
}
