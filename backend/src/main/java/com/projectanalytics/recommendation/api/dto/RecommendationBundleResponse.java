package com.projectanalytics.recommendation.api.dto;

import java.util.List;
import java.util.UUID;

/**
 * Scoped recommendation list for project, portfolio, or workspace surfaces.
 */
public record RecommendationBundleResponse(
        UUID scopeId,
        String scopeType,
        String scopeName,
        String executiveSummary,
        List<RecommendationResponse> recommendations
) {
}
