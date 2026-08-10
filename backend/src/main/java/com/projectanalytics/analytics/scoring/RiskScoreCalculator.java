package com.projectanalytics.analytics.scoring;

import com.projectanalytics.analytics.configuration.AnalyticsScoringProperties;
import com.projectanalytics.analytics.domain.ProjectScoringInput;
import com.projectanalytics.analytics.domain.ScoreFactor;
import com.projectanalytics.analytics.domain.ScoredMetric;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Deterministic Risk Score (0–100, higher is worse) from local project inputs.
 */
@Component
public class RiskScoreCalculator {

    private final AnalyticsScoringProperties properties;

    public RiskScoreCalculator(AnalyticsScoringProperties properties) {
        this.properties = properties;
    }

    public ScoredMetric calculate(ProjectScoringInput input) {
        List<ScoreFactor> factors = new ArrayList<>();
        List<String> explanations = new ArrayList<>();

        BigDecimal overdueRisk = overdueRisk(input, factors, explanations);
        BigDecimal scheduleRisk = scheduleRisk(input, factors, explanations);
        BigDecimal completionRisk = completionRisk(input, factors, explanations);

        BigDecimal score = ScoreMath.clamp(
                overdueRisk.multiply(ScoreMath.bd(properties.getRiskOverdueWorkPackageWeight()))
                        .add(scheduleRisk.multiply(ScoreMath.bd(properties.getRiskScheduleWeight())))
                        .add(completionRisk.multiply(ScoreMath.bd(properties.getRiskCompletionWeight())))
        );

        String level = riskLevel(score);
        return new ScoredMetric(score, level, ScoreMath.joinExplanations(explanations), List.copyOf(factors));
    }

    private BigDecimal overdueRisk(
            ProjectScoringInput input,
            List<ScoreFactor> factors,
            List<String> explanations
    ) {
        BigDecimal overdueRatio = ProgressMetrics.overdueRatio(input);
        // Scoring needs a numeric factor; null (no WPs) → 0 pressure, same as before.
        BigDecimal ratio = overdueRatio == null ? BigDecimal.ZERO : overdueRatio;
        BigDecimal score = ScoreMath.clamp(ratio.multiply(BigDecimal.valueOf(100)));
        factors.add(new ScoreFactor(
                "OVERDUE_WP",
                input.overdueWorkPackages() + " overdue of " + input.totalWorkPackages() + " work packages.",
                score,
                ratio
        ));
        if (input.overdueWorkPackages() > 0) {
            explanations.add(input.overdueWorkPackages() + " overdue work package(s) increase risk.");
        }
        if (input.highPriorityOpenWorkPackages() > 0) {
            explanations.add(input.highPriorityOpenWorkPackages() + " high-priority open work package(s) remain.");
        }
        return score;
    }

    private BigDecimal scheduleRisk(
            ProjectScoringInput input,
            List<ScoreFactor> factors,
            List<String> explanations
    ) {
        boolean pastEnd = input.endDate() != null
                && input.endDate().isBefore(input.asOfDate())
                && !"ARCHIVED".equalsIgnoreCase(nullSafe(input.status()));
        BigDecimal score = pastEnd ? BigDecimal.valueOf(90) : BigDecimal.valueOf(20);
        factors.add(new ScoreFactor(
                "SCHEDULE_RISK",
                pastEnd ? "Project end date is past." : "Project end date is not past.",
                score,
                null
        ));
        if (pastEnd) {
            explanations.add("Project end date is in the past while status is not archived.");
        }
        return score;
    }

    private BigDecimal completionRisk(
            ProjectScoringInput input,
            List<ScoreFactor> factors,
            List<String> explanations
    ) {
        double completion = ProgressMetrics.actualProgress(input).doubleValue() / 100.0;
        BigDecimal score = ScoreMath.clamp(BigDecimal.valueOf((1.0 - completion) * 100));
        factors.add(new ScoreFactor(
                "LOW_COMPLETION",
                String.format("Remaining incomplete work is approximately %.0f%%.", score),
                score,
                BigDecimal.valueOf(completion)
        ));
        if (completion < 0.4 && input.totalWorkPackages() > 0) {
            explanations.add("Low completion rate elevates delivery risk.");
        }
        if (explanations.isEmpty()) {
            explanations.add("Risk factors remain within moderate operational bounds.");
        }
        return score;
    }

    static String riskLevel(BigDecimal score) {
        double value = score.doubleValue();
        if (value >= 75) {
            return "Critical";
        }
        if (value >= 50) {
            return "High";
        }
        if (value >= 25) {
            return "Medium";
        }
        return "Low";
    }

    private static String nullSafe(String value) {
        return value == null ? "" : value;
    }
}
