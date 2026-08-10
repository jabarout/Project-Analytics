package com.projectanalytics.analytics.api.dto;

import java.math.BigDecimal;
import java.util.List;

public record ScoredMetricResponse(
        BigDecimal score,
        String label,
        String explanation,
        List<ScoreFactorResponse> factors
) {
}
