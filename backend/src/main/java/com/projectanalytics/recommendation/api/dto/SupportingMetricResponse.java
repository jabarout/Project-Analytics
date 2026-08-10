package com.projectanalytics.recommendation.api.dto;

/**
 * Measurable evidence referenced by a recommendation.
 */
public record SupportingMetricResponse(
        String code,
        String label,
        String value
) {
}
