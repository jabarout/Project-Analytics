package com.projectanalytics.analytics.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Full analytics result for one project (engine output before/after persistence).
 * Extended schedule / overdue fields are nullable when inputs are incomplete — never invented.
 */
public record ProjectAnalyticsSnapshot(
        UUID projectId,
        ScoredMetric health,
        ScoredMetric risk,
        ScoredMetric attention,
        /** Canonical actual progress % (WP-based when WPs exist). */
        BigDecimal completionPercentage,
        /** Schedule elapsed expected progress %; null when dates incomplete. */
        BigDecimal expectedProgress,
        /** actual − expected; negative = behind; null when expected unknown. */
        BigDecimal progressGap,
        /** overdue / total WPs (0–1); null when no WPs. */
        BigDecimal overdueRatio,
        /** Average days past due among overdue open WPs; null if none. */
        BigDecimal avgOverdueAgeDays,
        /** Max days past due among overdue open WPs; null if none. */
        Integer maxOverdueAgeDays,
        /** Days past end date (positive = late); null if no end date. From ProgressMetrics. */
        BigDecimal scheduleVariance,
        BigDecimal budgetVariance,
        Instant calculatedAt
) {
}
