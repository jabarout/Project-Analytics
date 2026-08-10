package com.projectanalytics.recommendation.application;

import com.projectanalytics.analytics.api.dto.ProjectAnalyticsResponse;
import com.projectanalytics.analytics.api.dto.TrendPointResponse;
import com.projectanalytics.recommendation.application.rules.RecommendationCandidate;
import com.projectanalytics.recommendation.application.rules.RecommendationRule;
import com.projectanalytics.recommendation.config.RecommendationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * Applies deterministic rules to analytics outputs. Does not calculate scores.
 */
@Component
public class RecommendationEngine {

    private final List<RecommendationRule> rules;
    private final RecommendationProperties properties;

    public RecommendationEngine(List<RecommendationRule> rules, RecommendationProperties properties) {
        this.rules = List.copyOf(rules);
        this.properties = properties;
    }

    public List<RecommendationCandidate> evaluateProject(
            UUID analyticsId,
            ProjectAnalyticsResponse analytics,
            List<TrendPointResponse> trends
    ) {
        List<RecommendationCandidate> candidates = new ArrayList<>();
        for (RecommendationRule rule : rules) {
            candidates.addAll(rule.evaluate(analyticsId, analytics, trends, properties));
        }
        candidates.sort(Comparator
                .comparing((RecommendationCandidate c) -> c.severity().ordinal())
                .thenComparing(RecommendationCandidate::ruleCode));
        int limit = Math.max(1, properties.getMaxPerProject());
        if (candidates.size() > limit) {
            return List.copyOf(candidates.subList(0, limit));
        }
        return List.copyOf(candidates);
    }
}
