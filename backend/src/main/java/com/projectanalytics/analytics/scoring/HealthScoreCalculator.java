package com.projectanalytics.analytics.scoring;

import com.projectanalytics.analytics.configuration.AnalyticsScoringProperties;
import com.projectanalytics.analytics.domain.ProjectScoringInput;
import com.projectanalytics.analytics.domain.ScoreFactor;
import com.projectanalytics.analytics.domain.ScoredMetric;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Deterministic Health Score (0–100, higher is better) from local project inputs.
 */
@Component
public class HealthScoreCalculator {

    private final AnalyticsScoringProperties properties;

    public HealthScoreCalculator(AnalyticsScoringProperties properties) {
        this.properties = properties;
    }

    public ScoredMetric calculate(ProjectScoringInput input) {
        List<ScoreFactor> factors = new ArrayList<>();
        List<String> explanations = new ArrayList<>();

        BigDecimal schedule = schedulePerformance(input, factors, explanations);
        BigDecimal delivery = deliveryPerformance(input, factors, explanations);
        BigDecimal overdue = overduePerformance(input, factors, explanations);

        BigDecimal score = ScoreMath.clamp(
                schedule.multiply(ScoreMath.bd(properties.getHealthScheduleWeight()))
                        .add(delivery.multiply(ScoreMath.bd(properties.getHealthDeliveryWeight())))
                        .add(overdue.multiply(ScoreMath.bd(properties.getHealthOverdueWeight())))
        );

        String label = healthLabel(score);
        return new ScoredMetric(score, label, ScoreMath.joinExplanations(explanations), List.copyOf(factors));
    }

    private BigDecimal schedulePerformance(
            ProjectScoringInput input,
            List<ScoreFactor> factors,
            List<String> explanations
    ) {
        BigDecimal expected = ProgressMetrics.expectedProgress(input);
        if (expected == null) {
            BigDecimal neutral = BigDecimal.valueOf(70);
            factors.add(new ScoreFactor("SCHEDULE", "Schedule dates incomplete; neutral schedule factor.", neutral, null));
            explanations.add("Schedule factor is neutral because start/end dates are incomplete.");
            return neutral;
        }

        BigDecimal actualPct = ProgressMetrics.actualProgress(input);
        double planned = expected.doubleValue() / 100.0;
        double actual = actualPct.doubleValue() / 100.0;

        double delta = actual - planned; // positive = ahead
        BigDecimal score = ScoreMath.clamp(BigDecimal.valueOf(70 + (delta * 100)));
        factors.add(new ScoreFactor(
                "SCHEDULE",
                String.format("Actual progress %.0f%% vs planned %.0f%% of schedule.", actual * 100, planned * 100),
                score,
                BigDecimal.valueOf(delta).setScale(4, RoundingMode.HALF_UP)
        ));
        if (delta < -0.1) {
            explanations.add(String.format(
                    "Schedule is behind: progress is %.0f%% while %.0f%% of the timeline has elapsed.",
                    actual * 100,
                    planned * 100
            ));
        } else if (delta > 0.05) {
            explanations.add("Schedule performance is ahead of the planned timeline.");
        } else {
            explanations.add("Schedule performance is roughly on track.");
        }
        return score;
    }

    private BigDecimal deliveryPerformance(
            ProjectScoringInput input,
            List<ScoreFactor> factors,
            List<String> explanations
    ) {
        BigDecimal completion = ProgressMetrics.actualProgress(input);
        if (input.totalWorkPackages() == 0) {
            factors.add(new ScoreFactor(
                    "DELIVERY",
                    input.progress() == null
                            ? "No work packages and no project progress field; delivery neutral-low."
                            : "No work packages; using project progress field.",
                    completion.compareTo(BigDecimal.ZERO) == 0 && input.progress() == null
                            ? BigDecimal.valueOf(50)
                            : completion,
                    input.progress()
            ));
            explanations.add(input.progress() == null
                    ? "Delivery factor is neutral-low because no work packages or progress are available."
                    : "Delivery factor uses project progress because no work packages are synchronized.");
            return completion.compareTo(BigDecimal.ZERO) == 0 && input.progress() == null
                    ? BigDecimal.valueOf(50)
                    : completion;
        }
        BigDecimal score = ScoreMath.clamp(completion);
        factors.add(new ScoreFactor(
                "DELIVERY",
                input.completedWorkPackages() + " of " + input.totalWorkPackages() + " work packages completed.",
                score,
                completion
        ));
        explanations.add(String.format(
                "Task completion is %.0f%% (%d/%d work packages).",
                score,
                input.completedWorkPackages(),
                input.totalWorkPackages()
        ));
        return score;
    }

    private BigDecimal overduePerformance(
            ProjectScoringInput input,
            List<ScoreFactor> factors,
            List<String> explanations
    ) {
        if (input.totalWorkPackages() == 0) {
            boolean projectOverdue = input.endDate() != null && input.endDate().isBefore(input.asOfDate())
                    && !"ARCHIVED".equalsIgnoreCase(nullSafe(input.status()));
            BigDecimal score = projectOverdue ? BigDecimal.valueOf(35) : BigDecimal.valueOf(85);
            factors.add(new ScoreFactor(
                    "OVERDUE",
                    projectOverdue ? "Project end date is in the past." : "Project end date is not overdue.",
                    score,
                    null
            ));
            explanations.add(projectOverdue
                    ? "Project end date is overdue."
                    : "No overdue signal from project end date.");
            return score;
        }
        BigDecimal overdueRatio = ProgressMetrics.overdueRatio(input);
        // totalWorkPackages > 0 in this branch → ratio is non-null
        BigDecimal ratio = overdueRatio == null ? BigDecimal.ZERO : overdueRatio;
        BigDecimal score = ScoreMath.clamp(BigDecimal.valueOf(100).subtract(ratio.multiply(BigDecimal.valueOf(100))));
        factors.add(new ScoreFactor(
                "OVERDUE",
                input.overdueWorkPackages() + " overdue work packages.",
                score,
                ratio
        ));
        if (input.overdueWorkPackages() > 0) {
            explanations.add(input.overdueWorkPackages() + " work package(s) are past due.");
        } else {
            explanations.add("No overdue work packages.");
        }
        return score;
    }

    static String healthLabel(BigDecimal score) {
        double value = score.doubleValue();
        if (value >= 90) {
            return "Excellent";
        }
        if (value >= 75) {
            return "Healthy";
        }
        if (value >= 60) {
            return "Moderate";
        }
        if (value >= 40) {
            return "At Risk";
        }
        return "Critical";
    }

    private static String nullSafe(String value) {
        return value == null ? "" : value;
    }
}
