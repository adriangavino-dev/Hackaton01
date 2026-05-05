package com.tuckersoft.tropelcare.sector;

import com.tuckersoft.tropelcare.sector.dto.SectorRequest;
import com.tuckersoft.tropelcare.sector.dto.SectorResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/sectors")
public class SectorController {

    private final SectorService service;

    public SectorController(SectorService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<SectorResponse> create(@Valid @RequestBody SectorRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(req));
    }

    @GetMapping
    public List<SectorResponse> list() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public SectorResponse get(@PathVariable Long id) {
        return service.findById(id);
    }
}
