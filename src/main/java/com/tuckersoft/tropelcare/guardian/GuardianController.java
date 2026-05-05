package com.tuckersoft.tropelcare.guardian;

import com.tuckersoft.tropelcare.guardian.dto.GuardianResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/guardians")
public class GuardianController {

    private final GuardianService service;

    public GuardianController(GuardianService service) {
        this.service = service;
    }

    @GetMapping
    public List<GuardianResponse> list() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public GuardianResponse get(@PathVariable Long id) {
        return service.findById(id);
    }
}
