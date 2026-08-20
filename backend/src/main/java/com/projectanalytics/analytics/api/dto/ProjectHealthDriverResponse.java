package com.projectanalytics.analytics.api.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Project Health delta across stored snapshots (last − first). Presentation aid for drivers.
 */
public record ProjectHealthDriverResponse(
        UUID projectId,
        String projectName,
        BigDecimal firstHealthScore,
        BigDecimal lastHealthScore,
        BigDecimal delta,
        Instant firstAt,
        Instant lastAt
) {
}
