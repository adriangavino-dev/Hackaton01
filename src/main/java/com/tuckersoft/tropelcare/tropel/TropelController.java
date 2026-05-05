package com.tuckersoft.tropelcare.tropel;

import com.tuckersoft.tropelcare.common.PageResponse;
import com.tuckersoft.tropelcare.tropel.dto.DiaryResponse;
import com.tuckersoft.tropelcare.tropel.dto.TropelCreateRequest;
import com.tuckersoft.tropelcare.tropel.dto.TropelResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/tropels")
public class TropelController {

    private final TropelService service;

    public TropelController(TropelService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<TropelResponse> create(@Valid @RequestBody TropelCreateRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(req));
    }

    @GetMapping
    public PageResponse<TropelResponse> list(
            @RequestParam(required = false) String species,
            @RequestParam(required = false) String vitalState,
            @RequestParam(required = false) Long sectorId,
            @RequestParam(required = false) Long guardianId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return service.search(species, vitalState, sectorId, guardianId, page, size);
    }

    @GetMapping("/{id}")
    public TropelResponse get(@PathVariable Long id) {
        return service.findById(id);
    }

    @GetMapping("/{id}/diary")
    public DiaryResponse diary(@PathVariable Long id) {
        return service.diary(id);
    }
}
