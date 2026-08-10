package com.projectanalytics.analytics.scoring;

import com.projectanalytics.analytics.domain.ProjectAnalyticsSnapshot;
import com.projectanalytics.analytics.domain.ProjectScoringInput;
import com.projectanalytics.analytics.domain.ScoredMetric;
import com.projectanalytics.common.exception.BusinessException;
import com.projectanalytics.common.exception.ErrorCode;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Single analytics engine used by project, portfolio, and workspace dashboards.
 * Score calculators live in this package; progress/schedule quantities come only from
 * {@link ProgressMetrics} — no parallel formulas here.
 */
@Service
public class ProjectAnalyticsEngine {

    private final HealthScoreCalculator healthScoreCalculator;
    private final RiskScoreCalculator riskScoreCalculator;
    private final AttentionScoreCalculator attentionScoreCalculator;

    public ProjectAnalyticsEngine(
            HealthScoreCalculator healthScoreCalculator,
            RiskScoreCalculator riskScoreCalculator,
            AttentionScoreCalculator attentionScoreCalculator
    ) {
        this.healthScoreCalculator = healthScoreCalculator;
        this.riskScoreCalculator = riskScoreCalculator;
        this.attentionScoreCalculator = attentionScoreCalculator;
    }

    public ProjectAnalyticsSnapshot score(ProjectScoringInput input) {
        try {
            ScoredMetric health = healthScoreCalculator.calculate(input);
            ScoredMetric risk = riskScoreCalculator.calculate(input);
            ScoredMetric attention = attentionScoreCalculator.calculate(input, health, risk);

            BigDecimal budgetVariance = null; // spent budget not available in local model yet

            return new ProjectAnalyticsSnapshot(
                    input.projectId(),
                    health,
                    risk,
                    attention,
                    ProgressMetrics.actualProgress(input),
                    ProgressMetrics.expectedProgress(input),
                    ProgressMetrics.progressGap(input),
                    ProgressMetrics.overdueRatio(input),
                    input.avgOverdueAgeDays(),
                    input.maxOverdueAgeDays(),
                    ProgressMetrics.scheduleVarianceDays(input),
                    budgetVariance,
                    Instant.now()
            );
        } catch (BusinessException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            throw new BusinessException(ErrorCode.ANALYTICS_001, ErrorCode.ANALYTICS_001.getDefaultMessage(), exception);
        }
    }
}
