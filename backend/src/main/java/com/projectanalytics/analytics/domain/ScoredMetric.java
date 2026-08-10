package com.projectanalytics.analytics.domain;

import java.math.BigDecimal;
import java.util.List;

/**
 * Score with status/level label and explanation factors.
 */
public record ScoredMetric(
        BigDecimal score,
        String label,
        String explanation,
        List<ScoreFactor> factors
) {
}
