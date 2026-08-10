package com.projectanalytics.analytics.api.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Read-model row for Project Explorer (M11B). Composes existing project + analytics fields only.
 * No scoring formulas.
 */
public record ExplorerProjectRowResponse(
        UUID projectId,
        UUID workspaceId,
        String name,
        String status,
        /** Canonical progress = analytics completion % when available. */
        BigDecimal progress,
        /** Schedule-based expected progress %; null when dates incomplete. */
        BigDecimal expectedProgress,
        /** actual − expected; negative = behind. */
        BigDecimal progressGap,
        BigDecimal budget,
        LocalDate startDate,
        LocalDate endDate,
        Instant synchronizedAt,
        BigDecimal healthScore,
        String healthStatus,
        BigDecimal riskScore,
        String riskLevel,
        BigDecimal attentionScore,
        String attentionLabel,
        boolean delayed,
        boolean critical,
        boolean needsAttention,
        long overdueWorkPackageCount,
        /** overdue / total WPs (0–1); null when unknown. */
        BigDecimal overdueRatio,
        /** Days past end date (positive = late); null if no end date. Reuses analytics.schedule_variance. */
        BigDecimal scheduleVariance,
        List<UUID> portfolioIds,
        List<String> portfolioNames,
        /** OpenProject project admin display name(s) from memberships (not WP assignees). */
        String projectAdmin,
        /**
         * Date used for upcoming-deadline filters:
         * project finish date when set, otherwise earliest open work-package due date.
         */
        LocalDate nextDeadline,
        /** "project" | "work_package" | null */
        String nextDeadlineSource
) {
}
