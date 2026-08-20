package com.projectanalytics.analytics.api.dto;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * One workspace recalculation wave: equal-weight mean of project Health scores
 * (same aggregation as {@code ScopeAnalyticsKpiResponse.averageHealthScore}).
 */
public record WorkspaceHealthTrendPointResponse(
        Instant calculatedAt,
        BigDecimal averageHealthScore,
        int sampleSize
) {
}
