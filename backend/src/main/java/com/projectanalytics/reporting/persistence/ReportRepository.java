package com.projectanalytics.reporting.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface ReportRepository extends JpaRepository<ReportEntity, UUID> {

    List<ReportEntity> findAllByOrderByGeneratedAtDesc();

    List<ReportEntity> findByGeneratedAtBefore(Instant cutoff);
}
