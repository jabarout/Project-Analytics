package com.projectanalytics.analytics.api.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ProjectAnalyticsResponse(
        UUID projectId,
        String projectName,
        ScoredMetricResponse health,
        ScoredMetricResponse risk,
        ScoredMetricResponse attention,
        /** Canonical actual progress % (WP-based when WPs exist). */
        BigDecimal completionPercentage,
        /** Schedule-based expected progress %; null when dates incomplete. */
        BigDecimal expectedProgress,
        /** actual − expected; negative = behind schedule. */
        BigDecimal progressGap,
        /** overdue / total WPs (0–1); null when no WPs. */
        BigDecimal overdueRatio,
        BigDecimal avgOverdueAgeDays,
        Integer maxOverdueAgeDays,
        /** Days past end date (positive = late); null if no end date. Existing field — do not add a parallel deadline metric. */
        BigDecimal scheduleVariance,
        BigDecimal budgetVariance,
        Instant calculatedAt
) {
}
