package com.tuckersoft.tropelcare.careresponse;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CareResponseRepository extends JpaRepository<CareResponse, Long> {
    Optional<CareResponse> findBySignalId(Long signalId);
}
