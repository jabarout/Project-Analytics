package com.projectanalytics.recommendation.application.rules;

import com.projectanalytics.recommendation.domain.RecommendationSeverity;

import java.util.List;
import java.util.UUID;

/**
 * In-memory recommendation produced by a deterministic rule before persistence.
 */
public record RecommendationCandidate(
        UUID projectId,
        String projectName,
        UUID analyticsId,
        String ruleCode,
        String title,
        String description,
        RecommendationSeverity severity,
        String explanation,
        String suggestedAction,
        List<SupportingMetric> supportingMetrics
) {
    public RecommendationCandidate {
        supportingMetrics = supportingMetrics == null ? List.of() : List.copyOf(supportingMetrics);
    }

    public record SupportingMetric(String code, String label, String value) {
    }
}
