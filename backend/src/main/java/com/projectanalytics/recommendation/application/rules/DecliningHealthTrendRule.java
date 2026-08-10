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
public class DecliningHealthTrendRule implements RecommendationRule {

    @Override
    public String code() {
        return "DECLINING_HEALTH_TREND";
    }

    @Override
    public List<RecommendationCandidate> evaluate(
            UUID analyticsId,
            ProjectAnalyticsResponse analytics,
            List<TrendPointResponse> trends,
            RecommendationProperties properties
    ) {
        if (trends == null || trends.size() < 2) {
            return List.of();
        }
        BigDecimal oldest = trends.getFirst().healthScore();
        BigDecimal newest = trends.getLast().healthScore();
        if (oldest == null || newest == null) {
            return List.of();
        }
        double decline = oldest.doubleValue() - newest.doubleValue();
        if (decline < properties.getHealthDeclinePoints()) {
            return List.of();
        }
        return List.of(new RecommendationCandidate(
                analytics.projectId(),
                analytics.projectName(),
                analyticsId,
                code(),
                "Investigate declining health trend",
                "Project \"" + analytics.projectName() + "\" shows deteriorating health across historical analytics snapshots.",
                RecommendationSeverity.MEDIUM,
                "Health declined by " + format(decline) + " points from " + oldest.toPlainString()
                        + " to " + newest.toPlainString() + " across "
                        + trends.size() + " snapshots (threshold "
                        + properties.getHealthDeclinePoints() + " points).",
                "Compare contributing health factors between recent recalculations and address the largest negative drivers first.",
                List.of(
                        metric("health_oldest", "Oldest snapshot health", oldest),
                        metric("health_newest", "Newest snapshot health", newest),
                        metric("health_decline", "Health decline (points)", format(decline)),
                        metric("snapshot_count", "Snapshot count", trends.size())
                )
        ));
    }

    private static String format(double value) {
        return BigDecimal.valueOf(value).stripTrailingZeros().toPlainString();
    }

    private static RecommendationCandidate.SupportingMetric metric(String code, String label, Object value) {
        return new RecommendationCandidate.SupportingMetric(code, label, value == null ? "—" : String.valueOf(value));
    }
}
