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
 * Attention Score (0–100, higher needs more management attention).
 * Combines inverted health, risk, and overdue pressure from local inputs.
 */
@Component
public class AttentionScoreCalculator {

    private final AnalyticsScoringProperties properties;

    public AttentionScoreCalculator(AnalyticsScoringProperties properties) {
        this.properties = properties;
    }

    public ScoredMetric calculate(
            ProjectScoringInput input,
            ScoredMetric health,
            ScoredMetric risk
    ) {
        List<ScoreFactor> factors = new ArrayList<>();
        List<String> explanations = new ArrayList<>();

        BigDecimal invertedHealth = ScoreMath.clamp(BigDecimal.valueOf(100).subtract(health.score()));
        factors.add(new ScoreFactor(
                "HEALTH_PRESSURE",
                "Attention contribution from health (" + health.label() + ").",
                invertedHealth,
                health.score()
        ));

        factors.add(new ScoreFactor(
                "RISK_PRESSURE",
                "Attention contribution from risk (" + risk.label() + ").",
                risk.score(),
                risk.score()
        ));

        BigDecimal overduePressure = BigDecimal.ZERO;
        if (input.overdueWorkPackages() > 0 || (input.endDate() != null && input.endDate().isBefore(input.asOfDate()))) {
            overduePressure = BigDecimal.valueOf(80);
            explanations.add("Overdue schedule signals require management attention.");
        } else {
            overduePressure = BigDecimal.valueOf(15);
        }
        factors.add(new ScoreFactor(
                "OVERDUE_PRESSURE",
                "Overdue-driven attention pressure.",
                overduePressure,
                BigDecimal.valueOf(input.overdueWorkPackages())
        ));

        BigDecimal score = ScoreMath.clamp(
                invertedHealth.multiply(ScoreMath.bd(properties.getAttentionHealthWeight()))
                        .add(risk.score().multiply(ScoreMath.bd(properties.getAttentionRiskWeight())))
                        .add(overduePressure.multiply(ScoreMath.bd(properties.getAttentionOverdueWeight())))
        );

        explanations.add("Attention prioritizes projects with weaker health and higher risk.");
        if (health.score().doubleValue() < 60) {
            explanations.add("Health is below moderate threshold.");
        }
        if (risk.score().doubleValue() >= 50) {
            explanations.add("Risk is elevated.");
        }

        String label = attentionLabel(score);
        return new ScoredMetric(score, label, ScoreMath.joinExplanations(explanations), List.copyOf(factors));
    }

    public static String attentionLabel(BigDecimal score) {
        double value = score.doubleValue();
        if (value >= 75) {
            return "Urgent";
        }
        if (value >= 50) {
            return "Elevated";
        }
        if (value >= 25) {
            return "Watch";
        }
        return "Normal";
    }
}
