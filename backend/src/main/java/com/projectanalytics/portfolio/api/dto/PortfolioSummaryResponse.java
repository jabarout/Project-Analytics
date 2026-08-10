package com.projectanalytics.portfolio.api.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record PortfolioSummaryResponse(
        UUID id,
        UUID workspaceId,
        String name,
        String description,
        BigDecimal healthScore,
        BigDecimal attentionScore,
        long totalProjects,
        long activeProjects
) {
}
