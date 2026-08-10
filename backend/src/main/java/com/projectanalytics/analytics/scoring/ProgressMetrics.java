package com.projectanalytics.analytics.scoring;

import com.projectanalytics.analytics.domain.ProjectScoringInput;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.temporal.ChronoUnit;

/**
 * Single source of truth for progress and schedule-related calculations.
 * <p>
 * Health / Risk / Attention calculators and {@link ProjectAnalyticsEngine} must call these
 * helpers instead of re-deriving ratios, elapsed schedule, or deadline day counts.
 * Do not add parallel metric classes for the same quantities.
 */
public final class ProgressMetrics {

    private ProgressMetrics() {
    }

    /**
     * Canonical actual progress (0–100). Stored as {@code completionPercentage}.
     * <ol>
     *   <li>When work packages exist: completed / total × 100</li>
     *   <li>Else when OpenProject project.progress is set: that value (clamped)</li>
     *   <li>Else: 0</li>
     * </ol>
     */
    public static BigDecimal actualProgress(ProjectScoringInput input) {
        if (input.totalWorkPackages() > 0) {
            return ScoreMath.ratio(input.completedWorkPackages(), input.totalWorkPackages())
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP);
        }
        if (input.progress() != null) {
            return ScoreMath.clamp(input.progress());
        }
        return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Expected progress from schedule elapsed (0–100), or null when start/end are incomplete.
     * Before start → 0; after end → 100; otherwise elapsed/total × 100.
     */
    public static BigDecimal expectedProgress(ProjectScoringInput input) {
        if (input.startDate() == null || input.endDate() == null
                || !input.endDate().isAfter(input.startDate())) {
            return null;
        }
        long totalDays = ChronoUnit.DAYS.between(input.startDate(), input.endDate());
        if (totalDays <= 0) {
            return null;
        }
        long elapsedDays = ChronoUnit.DAYS.between(input.startDate(), input.asOfDate());
        if (elapsedDays <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        if (elapsedDays >= totalDays) {
            return BigDecimal.valueOf(100).setScale(2, RoundingMode.HALF_UP);
        }
        return BigDecimal.valueOf(elapsedDays)
                .divide(BigDecimal.valueOf(totalDays), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * actual − expected. Negative means behind schedule. Null when expected cannot be computed.
     */
    public static BigDecimal progressGap(ProjectScoringInput input) {
        BigDecimal expected = expectedProgress(input);
        if (expected == null) {
            return null;
        }
        return actualProgress(input).subtract(expected).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Overdue work packages / total work packages (0–1 scale), or null when no WPs.
     */
    public static BigDecimal overdueRatio(ProjectScoringInput input) {
        if (input.totalWorkPackages() <= 0) {
            return null;
        }
        return ScoreMath.ratio(input.overdueWorkPackages(), input.totalWorkPackages());
    }

    /**
     * Existing schedule variance in days: positive = past project end (late).
     * Null when end date is missing. Prefer this field over inventing a second deadline metric.
     */
    public static BigDecimal scheduleVarianceDays(ProjectScoringInput input) {
        if (input.endDate() == null) {
            return null;
        }
        long days = ChronoUnit.DAYS.between(input.endDate(), input.asOfDate());
        return BigDecimal.valueOf(days).setScale(2, RoundingMode.HALF_UP);
    }
}
