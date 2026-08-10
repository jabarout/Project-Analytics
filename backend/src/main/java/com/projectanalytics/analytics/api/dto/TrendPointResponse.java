package com.projectanalytics.analytics.api.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record TrendPointResponse(
        Instant calculatedAt,
        BigDecimal healthScore,
        BigDecimal riskScore,
        BigDecimal attentionScore,
        BigDecimal completionPercentage
) {
}
