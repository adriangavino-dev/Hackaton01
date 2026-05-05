package com.tuckersoft.tropelcare.guardian;

import com.tuckersoft.tropelcare.common.ApiException;
import com.tuckersoft.tropelcare.guardian.dto.GuardianResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class GuardianService {

    private final GuardianRepository guardians;

    public GuardianService(GuardianRepository guardians) {
        this.guardians = guardians;
    }

    public List<GuardianResponse> findAll() {
        return guardians.findAll().stream().map(GuardianResponse::from).toList();
    }

    public GuardianResponse findById(Long id) {
        Guardian g = guardians.findById(id)
                .orElseThrow(() -> ApiException.notFound("No existe un guardian con id " + id));
        return GuardianResponse.from(g);
    }
}
