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
public class CriticalHealthRule implements RecommendationRule {

    @Override
    public String code() {
        return "CRITICAL_HEALTH";
    }

    @Override
    public List<RecommendationCandidate> evaluate(
            UUID analyticsId,
            ProjectAnalyticsResponse analytics,
            List<TrendPointResponse> trends,
            RecommendationProperties properties
    ) {
        BigDecimal health = analytics.health().score();
        if (health == null || health.doubleValue() >= properties.getCriticalHealthThreshold()) {
            return List.of();
        }
        return List.of(new RecommendationCandidate(
                analytics.projectId(),
                analytics.projectName(),
                analyticsId,
                code(),
                "Address critical project health",
                "Project \"" + analytics.projectName() + "\" has a health score below the critical threshold and needs recovery planning.",
                RecommendationSeverity.CRITICAL,
                "Health score is " + health.toPlainString() + " (threshold "
                        + properties.getCriticalHealthThreshold() + "). Analytics explanation: "
                        + nullSafe(analytics.health().explanation()),
                "Review schedule, delivery, and overdue work packages; prioritize recovery actions and re-check analytics after the next sync.",
                List.of(
                        metric("health_score", "Health score", health),
                        metric("health_label", "Health label", analytics.health().label()),
                        metric("threshold", "Critical health threshold", properties.getCriticalHealthThreshold())
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
