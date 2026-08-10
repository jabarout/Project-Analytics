package com.projectanalytics.recommendation.application.rules;

import com.projectanalytics.analytics.api.dto.ProjectAnalyticsResponse;
import com.projectanalytics.analytics.api.dto.TrendPointResponse;
import com.projectanalytics.recommendation.config.RecommendationProperties;

import java.util.List;
import java.util.UUID;

/**
 * Deterministic recommendation rule. Consumes analytics DTOs only — no scoring.
 */
public interface RecommendationRule {

    String code();

    List<RecommendationCandidate> evaluate(
            UUID analyticsId,
            ProjectAnalyticsResponse analytics,
            List<TrendPointResponse> trends,
            RecommendationProperties properties
    );
}
