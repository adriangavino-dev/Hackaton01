package com.tuckersoft.tropelcare.guardian;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface GuardianRepository extends JpaRepository<Guardian, Long> {
    Optional<Guardian> findByEmail(String email);
    boolean existsByEmail(String email);
}
