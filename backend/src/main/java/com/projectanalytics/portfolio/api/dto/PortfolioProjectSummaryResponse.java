package com.projectanalytics.portfolio.api.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record PortfolioProjectSummaryResponse(
        UUID id,
        Long openProjectId,
        String name,
        String status,
        BigDecimal budget,
        BigDecimal progress,
        LocalDate startDate,
        LocalDate endDate,
        Instant synchronizedAt
) {
}
