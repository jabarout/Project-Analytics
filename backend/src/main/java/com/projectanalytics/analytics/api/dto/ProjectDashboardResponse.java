package com.projectanalytics.analytics.api.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record ProjectDashboardResponse(
        UUID projectId,
        String projectName,
        UUID workspaceId,
        UUID portfolioId,
        String status,
        BigDecimal progress,
        BigDecimal budget,
        LocalDate startDate,
        LocalDate endDate,
        Instant synchronizedAt,
        ProjectAnalyticsResponse analytics,
        List<TrendPointResponse> trends
) {
}
