package com.projectanalytics.portfolio.api.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Portfolio KPIs derived exclusively from local synchronized PostgreSQL data.
 * Full Health/Risk/Attention algorithms belong to the Analytics Engine (M5);
 * M4 exposes operational aggregates and stored score columns when present.
 */
public record PortfolioKpiResponse(
        UUID portfolioId,
        long totalProjects,
        long activeProjects,
        long archivedProjects,
        long overdueProjects,
        long totalWorkPackages,
        long overdueWorkPackages,
        BigDecimal totalBudget,
        BigDecimal averageProgress,
        BigDecimal healthScore,
        BigDecimal attentionScore,
        Instant lastSynchronizedAt
) {
}
