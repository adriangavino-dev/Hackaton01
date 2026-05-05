package com.tuckersoft.tropelcare.tropel;

import com.tuckersoft.tropelcare.common.ApiException;
import com.tuckersoft.tropelcare.guardian.Guardian;
import com.tuckersoft.tropelcare.guardian.GuardianRepository;
import com.tuckersoft.tropelcare.sector.Sector;
import com.tuckersoft.tropelcare.sector.SectorRepository;
import com.tuckersoft.tropelcare.signal.TropelSignalRepository;
import com.tuckersoft.tropelcare.tropel.dto.TropelCreateRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TropelServiceTest {

    @Mock TropelRepository tropels;
    @Mock SectorRepository sectors;
    @Mock GuardianRepository guardians;
    @Mock TropelSignalRepository signals;

    @InjectMocks TropelService service;

    @Test
    void registeringInFullSector_throwsBadRequest() {
        Sector full = new Sector();
        full.setId(1L);
        full.setSectorCode("SECTOR-7");
        full.setCapacity(1);
        full.setCurrentLoad(1);

        Guardian g = new Guardian();
        g.setId(1L);

        when(tropels.existsByName("BipBop")).thenReturn(false);
        when(sectors.findById(1L)).thenReturn(Optional.of(full));
        when(guardians.findById(1L)).thenReturn(Optional.of(g));

        TropelCreateRequest req = new TropelCreateRequest("BipBop", "GLITCHY", 1L, 1L);

        assertThatThrownBy(() -> service.create(req))
                .isInstanceOfSatisfying(ApiException.class,
                        ex -> assertThat(ex.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST))
                .hasMessageContaining("lleno");

        verify(tropels, never()).save(any());
    }
}
