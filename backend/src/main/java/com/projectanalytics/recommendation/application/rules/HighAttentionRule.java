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
public class HighAttentionRule implements RecommendationRule {

    @Override
    public String code() {
        return "HIGH_ATTENTION";
    }

    @Override
    public List<RecommendationCandidate> evaluate(
            UUID analyticsId,
            ProjectAnalyticsResponse analytics,
            List<TrendPointResponse> trends,
            RecommendationProperties properties
    ) {
        BigDecimal attention = analytics.attention().score();
        if (attention == null || attention.doubleValue() < properties.getHighAttentionThreshold()) {
            return List.of();
        }
        return List.of(new RecommendationCandidate(
                analytics.projectId(),
                analytics.projectName(),
                analyticsId,
                code(),
                "Prioritize executive attention",
                "Project \"" + analytics.projectName() + "\" ranks high on attention and should appear in management focus lists.",
                RecommendationSeverity.HIGH,
                "Attention score is " + attention.toPlainString() + " (threshold "
                        + properties.getHighAttentionThreshold() + "). Analytics explanation: "
                        + nullSafe(analytics.attention().explanation()),
                "Schedule a focused review of health and risk drivers; confirm blockers and next decisions with the project owner.",
                List.of(
                        metric("attention_score", "Attention score", attention),
                        metric("attention_label", "Attention label", analytics.attention().label()),
                        metric("threshold", "High attention threshold", properties.getHighAttentionThreshold())
                )
        ));
    }

    private static RecommendationCandidate.SupportingMetric metric(String code, String label, Object value) {
        return new RecommendationCandidate.SupportingMetric(code, label, value == null ? "—" : String.valueOf(value));
    }

    private static String nullSafe(String value) {
        return value == null ? "n/a" : value;
    }
}
