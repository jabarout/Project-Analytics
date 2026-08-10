package com.projectanalytics.analytics.api.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Aggregated analytics KPIs for a workspace or portfolio scope.
 * Built only by the analytics module from stored project scores + local operational counts.
 * Progress/schedule aggregates average stored fields from {@code ProgressMetrics} — no parallel formulas.
 */
public record ScopeAnalyticsKpiResponse(
        UUID scopeId,
        String scopeType,
        long totalProjects,
        long activeProjects,
        long criticalProjects,
        long highAttentionProjects,
        long overdueProjects,
        long totalWorkPackages,
        BigDecimal averageHealthScore,
        BigDecimal averageRiskScore,
        BigDecimal averageAttentionScore,
        /** Average of stored completion % (canonical actual progress). */
        BigDecimal averageCompletion,
        /** Average of stored expected progress %; null if no projects have schedule dates. */
        BigDecimal averageExpectedProgress,
        /** Average of stored progress gap (actual − expected); negative = behind. */
        BigDecimal averageProgressGap,
        /** Projects with progressGap &lt; 0. */
        long projectsBehindSchedule,
        /** Average of stored overdue ratio (0–1); null if none have WPs. */
        BigDecimal averageOverdueRatio,
        /** Projects with overdueRatio &gt; 0. */
        long projectsWithOverdueWorkPackages,
        BigDecimal totalBudget,
        Instant lastCalculatedAt
) {
}
