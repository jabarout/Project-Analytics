package com.projectanalytics.analytics.domain;

import java.math.BigDecimal;

/**
 * Single explainable factor contributing to a score.
 */
public record ScoreFactor(
        String code,
        String description,
        BigDecimal contribution,
        BigDecimal rawValue
) {
}
