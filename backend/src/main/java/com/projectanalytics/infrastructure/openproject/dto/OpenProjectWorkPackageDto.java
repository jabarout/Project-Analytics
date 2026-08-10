package com.projectanalytics.infrastructure.openproject.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/**
 * Normalized OpenProject work package payload used by the mapping layer.
 */
public record OpenProjectWorkPackageDto(
        long id,
        long projectId,
        String subject,
        String type,
        String status,
        String priority,
        String assignee,
        BigDecimal estimatedHours,
        BigDecimal spentHours,
        LocalDate dueDate,
        Instant updatedAt
) {
}
