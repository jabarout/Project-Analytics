package com.projectanalytics.portfolio.api.dto;

import java.util.List;
import java.util.UUID;

/**
 * Ready-to-display portfolio dashboard DTO (local data only).
 */
public record PortfolioDashboardResponse(
        UUID portfolioId,
        String portfolioName,
        UUID workspaceId,
        PortfolioKpiResponse kpis,
        String executiveSummary,
        List<PortfolioProjectSummaryResponse> activeProjects,
        List<PortfolioProjectSummaryResponse> overdueProjects,
        List<String> operationalInsights
) {
}
