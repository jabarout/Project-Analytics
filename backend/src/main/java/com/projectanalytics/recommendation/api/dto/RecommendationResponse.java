package com.projectanalytics.recommendation.api.dto;

import com.projectanalytics.recommendation.domain.RecommendationSeverity;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Recommendation module DTO — independent from analytics scoring DTOs.
 */
public record RecommendationResponse(
        UUID id,
        UUID projectId,
        String projectName,
        UUID analyticsId,
        String ruleCode,
        String title,
        String description,
        RecommendationSeverity severity,
        String explanation,
        String suggestedAction,
        int priorityRank,
        List<SupportingMetricResponse> supportingMetrics,
        Instant generatedAt
) {
}
