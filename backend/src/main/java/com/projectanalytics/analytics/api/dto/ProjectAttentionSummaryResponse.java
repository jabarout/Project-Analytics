package com.projectanalytics.analytics.api.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record ProjectAttentionSummaryResponse(
        UUID projectId,
        String projectName,
        String status,
        BigDecimal healthScore,
        String healthStatus,
        BigDecimal riskScore,
        String riskLevel,
        BigDecimal attentionScore,
        String attentionLabel
) {
}
