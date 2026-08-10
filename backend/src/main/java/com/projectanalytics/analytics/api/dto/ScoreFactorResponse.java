package com.projectanalytics.analytics.api.dto;

import java.math.BigDecimal;

public record ScoreFactorResponse(
        String code,
        String description,
        BigDecimal contribution,
        BigDecimal rawValue
) {
}
