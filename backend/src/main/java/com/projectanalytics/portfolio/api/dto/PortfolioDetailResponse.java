package com.projectanalytics.portfolio.api.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record PortfolioDetailResponse(
        UUID id,
        UUID workspaceId,
        String name,
        String description,
        BigDecimal healthScore,
        BigDecimal attentionScore,
        long totalProjects,
        long activeProjects,
        List<PortfolioProjectSummaryResponse> projects
) {
}
