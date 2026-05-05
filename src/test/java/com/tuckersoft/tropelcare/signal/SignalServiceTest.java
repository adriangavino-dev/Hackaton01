package com.tuckersoft.tropelcare.signal;

import com.tuckersoft.tropelcare.ai.ClassificationResult;
import com.tuckersoft.tropelcare.ai.GithubModelsClient;
import com.tuckersoft.tropelcare.ai.SignalClassifier;
import com.tuckersoft.tropelcare.careresponse.CareResponse;
import com.tuckersoft.tropelcare.careresponse.CareResponseRepository;
import com.tuckersoft.tropelcare.common.ApiException;
import com.tuckersoft.tropelcare.guardian.Guardian;
import com.tuckersoft.tropelcare.guardian.GuardianRepository;
import com.tuckersoft.tropelcare.sector.Sector;
import com.tuckersoft.tropelcare.sector.SectorRepository;
import com.tuckersoft.tropelcare.signal.dto.SignalCreateRequest;
import com.tuckersoft.tropelcare.signal.dto.SignalResponse;
import com.tuckersoft.tropelcare.tropel.Tropel;
import com.tuckersoft.tropelcare.tropel.TropelRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SignalServiceTest {

    @Mock TropelSignalRepository signals;
    @Mock TropelRepository tropels;
    @Mock GuardianRepository guardians;
    @Mock SectorRepository sectors;
    @Mock CareResponseRepository careResponses;
    @Mock SignalClassifier classifier;
    @Mock ApplicationEventPublisher events;

    @InjectMocks SignalService service;

    private Guardian guardian;
    private Sector sector;
    private Tropel tropel;

    @BeforeEach
    void setUp() {
        guardian = new Guardian();
        guardian.setId(1L);
        guardian.setDisplayName("Cameron Walker");
        guardian.setEmail("cameron@tuckersoft.com");
        guardian.setNotificationEmail("real@gmail.com");

        sector = new Sector();
        sector.setId(1L);
        sector.setSectorCode("SECTOR-7");
        sector.setClimate("RETRO_ARCADE");
        sector.setCapacity(3);
        sector.setCurrentLoad(1);
        sector.setStabilityLevel(100);

        tropel = new Tropel();
        tropel.setId(1L);
        tropel.setName("BipBop");
        tropel.setSpecies("GLITCHY");
        tropel.setVitalState("ESTABLE");
        tropel.setEnergyLevel(80);
        tropel.setChaosIndex(10);
        tropel.setMutationStage(0);
        tropel.setSector(sector);
        tropel.setGuardian(guardian);
        tropel.setCreatedAt(Instant.now());
        tropel.setUpdatedAt(Instant.now());
    }

    private void stubLookups() {
        when(tropels.findById(1L)).thenReturn(Optional.of(tropel));
        when(guardians.findById(1L)).thenReturn(Optional.of(guardian));
        when(signals.save(any(TropelSignal.class))).thenAnswer(inv -> {
            TropelSignal s = inv.getArgument(0);
            s.setId(42L);
            return s;
        });
    }

    private SignalCreateRequest req() {
        return new SignalCreateRequest(1L, 1L, "sensor-1",
                "BipBop lleva 3 ciclos sin recibir nutrientes y muerde los bordes.");
    }

    // 1) Happy path: AI returns valid JSON.
    @Test
    void happyPath_persistsClassifiedFields_andStatusRecibida() {
        stubLookups();
        when(classifier.classify(anyString())).thenReturn(new ClassificationResult(
                "HAMBRE", "MODERADO", "Laboratorio de Nutricion",
                "Enviar paquete de nutrientes.", null, false));

        SignalResponse resp = service.create(req());

        assertThat(resp.signalType()).isEqualTo("HAMBRE");
        assertThat(resp.severity()).isEqualTo("MODERADO");
        assertThat(resp.assignedUnit()).isEqualTo("Laboratorio de Nutricion");
        assertThat(resp.recommendedAction()).isEqualTo("Enviar paquete de nutrientes.");
        assertThat(resp.status()).isEqualTo("RECIBIDA");

        ArgumentCaptor<CareResponse> cap = ArgumentCaptor.forClass(CareResponse.class);
        verify(careResponses).save(cap.capture());
        assertThat(cap.getValue().getResponseCode()).isEqualTo("DISPATCH_NUTRIENT_PACK");
    }

    // 2) AI returns JSON wrapped in extra text -> extraction works.
    @Test
    void messyJson_isExtractedSuccessfully() {
        String messy = """
                Aqui va la respuesta del modelo:
                ```json
                {"signalType":"FUGA","severity":"GRAVE","assignedUnit":"Equipo de Contencion","recommendedAction":"Bloquear sector"}
                ```
                Espero que ayude.
                """;
        String json = GithubModelsClient.extractJson(messy);

        assertThat(json).isNotNull();
        assertThat(json).startsWith("{").endsWith("}");
        assertThat(json).contains("\"signalType\":\"FUGA\"");
    }

    // 3) AI throws -> fallback values, status ERROR, no exception propagated.
    @Test
    void aiThrows_appliesFallback_andDoesNotPropagate() {
        stubLookups();
        when(classifier.classify(anyString())).thenReturn(ClassificationResult.ofFallback());

        SignalResponse resp = service.create(req());

        assertThat(resp.status()).isEqualTo("ERROR");
        assertThat(resp.signalType()).isEqualTo("SENAL_CORRUPTA");
        assertThat(resp.severity()).isEqualTo("LEVE");
        assertThat(resp.assignedUnit()).isEqualTo("Archivo de Senales");

        ArgumentCaptor<CareResponse> cap = ArgumentCaptor.forClass(CareResponse.class);
        verify(careResponses).save(cap.capture());
        assertThat(cap.getValue().getResponseCode()).isEqualTo("ARCHIVE_AND_IGNORE");

        verify(events, never()).publishEvent(any());
    }

    // 4) CRITICO severity: chaosIndex +45 (capped at 100), mutationStage +1 (capped at 5).
    @Test
    void criticoSeverity_appliesDeltasAndClamps() {
        tropel.setChaosIndex(70);
        tropel.setMutationStage(5);
        stubLookups();
        when(classifier.classify(anyString())).thenReturn(new ClassificationResult(
                "MUTACION", "CRITICO", "Division Genetica",
                "Aislar y observar.", null, false));

        service.create(req());

        assertThat(tropel.getChaosIndex()).isEqualTo(100); // 70+45 clamped
        assertThat(tropel.getMutationStage()).isEqualTo(5); // 5+1 clamped
        assertThat(tropel.getEnergyLevel()).isEqualTo(50); // 80-30
        assertThat(tropel.getVitalState()).isEqualTo("CRITICO"); // chaosIndex>=80
    }

    // 5) Event publish: exactly once on success, never on fallback.
    @Test
    void eventPublish_onceOnSuccess_neverOnFallback() {
        stubLookups();
        when(classifier.classify(anyString())).thenReturn(new ClassificationResult(
                "HAMBRE", "LEVE", "Laboratorio de Nutricion", "x", null, false));
        service.create(req());
        verify(events, times(1)).publishEvent(any(TropelSignalCreatedEvent.class));

        reset(events);
        when(classifier.classify(anyString())).thenReturn(ClassificationResult.ofFallback());
        service.create(req());
        verify(events, never()).publishEvent(any());
    }

    // 6) GuardianId mismatch -> 400.
    @Test
    void guardianIdMismatch_throwsBadRequest() {
        Guardian other = new Guardian();
        other.setId(2L);
        tropel.setGuardian(other);
        when(tropels.findById(1L)).thenReturn(Optional.of(tropel));
        when(guardians.findById(1L)).thenReturn(Optional.of(guardian));

        assertThatThrownBy(() -> service.create(req()))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("guardianId");

        verify(events, never()).publishEvent(any());
        verify(signals, never()).save(any());
    }
}
