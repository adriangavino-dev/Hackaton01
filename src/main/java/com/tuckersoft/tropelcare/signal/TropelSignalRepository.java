package com.tuckersoft.tropelcare.signal;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface TropelSignalRepository extends JpaRepository<TropelSignal, Long>, JpaSpecificationExecutor<TropelSignal> {

    @Override
    Page<TropelSignal> findAll(Specification<TropelSignal> spec, Pageable pageable);

    List<TropelSignal> findByTropelIdAndPersonalityNoteIsNotNullOrderByCreatedAtDesc(Long tropelId);
}
