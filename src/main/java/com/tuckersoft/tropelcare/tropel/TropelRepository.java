package com.tuckersoft.tropelcare.tropel;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface TropelRepository extends JpaRepository<Tropel, Long>, JpaSpecificationExecutor<Tropel> {
    boolean existsByName(String name);

    @Override
    Page<Tropel> findAll(Specification<Tropel> spec, Pageable pageable);
}
