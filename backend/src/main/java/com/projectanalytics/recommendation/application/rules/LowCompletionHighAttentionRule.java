package com.projectanalytics.recommendation.application.rules;

import com.projectanalytics.analytics.api.dto.ProjectAnalyticsResponse;
import com.projectanalytics.analytics.api.dto.TrendPointResponse;
import com.projectanalytics.recommendation.config.RecommendationProperties;
import com.projectanalytics.recommendation.domain.RecommendationSeverity;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Component
public class LowCompletionHighAttentionRule implements RecommendationRule {

    @Override
    public String code() {
        return "LOW_COMPLETION_HIGH_ATTENTION";
    }

    @Override
    public List<RecommendationCandidate> evaluate(
            UUID analyticsId,
            ProjectAnalyticsResponse analytics,
            List<TrendPointResponse> trends,
            RecommendationProperties properties
    ) {
        BigDecimal completion = analytics.completionPercentage();
        BigDecimal attention = analytics.attention().score();
        if (completion == null || attention == null) {
            return List.of();
        }
        if (completion.doubleValue() >= properties.getLowCompletionThreshold()
                || attention.doubleValue() < properties.getHighAttentionThreshold()) {
            return List.of();
        }
        return List.of(new RecommendationCandidate(
                analytics.projectId(),
                analytics.projectName(),
                analyticsId,
                code(),
                "Accelerate delivery on high-attention work",
                "Project \"" + analytics.projectName()
                        + "\" combines low completion with high attention, indicating stalled high-priority delivery.",
                RecommendationSeverity.MEDIUM,
                "Completion is " + completion.toPlainString() + "% (threshold "
                        + properties.getLowCompletionThreshold() + "%) while attention is "
                        + attention.toPlainString() + " (threshold "
                        + properties.getHighAttentionThreshold() + ").",
                "Re-plan remaining work packages, remove blockers, and reassess attention after measurable completion gains.",
                List.of(
                        metric("completion_percentage", "Completion %", completion),
                        metric("attention_score", "Attention score", attention),
                        metric("completion_threshold", "Low completion threshold", properties.getLowCompletionThreshold()),
                        metric("attention_threshold", "High attention threshold", properties.getHighAttentionThreshold())
                )
        ));
    }

    private static RecommendationCandidate.SupportingMetric metric(String code, String label, Object value) {
        return new RecommendationCandidate.SupportingMetric(code, label, value == null ? "—" : String.valueOf(value));
    }
}
