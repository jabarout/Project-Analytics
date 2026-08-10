package com.projectanalytics.infrastructure.openproject.dto;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Normalized OpenProject project payload used by the mapping layer.
 */
public record OpenProjectProjectDto(
        long id,
        String name,
        String description,
        String status,
        LocalDate startDate,
        LocalDate endDate,
        Instant updatedAt,
        /** OpenProject membership principal title(s) with Project admin (or similar) role. */
        String adminName
) {
}
