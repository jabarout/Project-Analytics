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
public class ElevatedRiskRule implements RecommendationRule {

    @Override
    public String code() {
        return "ELEVATED_RISK";
    }

    @Override
    public List<RecommendationCandidate> evaluate(
            UUID analyticsId,
            ProjectAnalyticsResponse analytics,
            List<TrendPointResponse> trends,
            RecommendationProperties properties
    ) {
        BigDecimal risk = analytics.risk().score();
        if (risk == null || risk.doubleValue() < properties.getHighRiskThreshold()) {
            return List.of();
        }
        return List.of(new RecommendationCandidate(
                analytics.projectId(),
                analytics.projectName(),
                analyticsId,
                code(),
                "Mitigate elevated project risk",
                "Project \"" + analytics.projectName() + "\" shows elevated risk that warrants explicit mitigation ownership.",
                RecommendationSeverity.HIGH,
                "Risk score is " + risk.toPlainString() + " (threshold "
                        + properties.getHighRiskThreshold() + "). Analytics explanation: "
                        + nullSafe(analytics.risk().explanation()),
                "Identify top risk drivers from the analytics factors, assign mitigation owners, and track risk score after the next analytics recalculation.",
                List.of(
                        metric("risk_score", "Risk score", risk),
                        metric("risk_label", "Risk level", analytics.risk().label()),
                        metric("threshold", "High risk threshold", properties.getHighRiskThreshold())
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
