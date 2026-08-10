package com.projectanalytics.analytics.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Normalized local inputs for deterministic scoring. Built only from PostgreSQL data.
 * Aging fields are null when no overdue open work packages exist.
 */
public record ProjectScoringInput(
        UUID projectId,
        String projectName,
        String status,
        BigDecimal progress,
        BigDecimal budget,
        LocalDate startDate,
        LocalDate endDate,
        long totalWorkPackages,
        long completedWorkPackages,
        long openWorkPackages,
        long overdueWorkPackages,
        long highPriorityOpenWorkPackages,
        /** Average days past due among overdue open WPs; null if none. */
        BigDecimal avgOverdueAgeDays,
        /** Max days past due among overdue open WPs; null if none. */
        Integer maxOverdueAgeDays,
        LocalDate asOfDate
) {
}
