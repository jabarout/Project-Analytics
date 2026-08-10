package com.projectanalytics.recommendation.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configurable recommendation trigger thresholds.
 * Thresholds interpret existing analytics scores — they are not new scoring formulas.
 */
@ConfigurationProperties(prefix = "projectanalytics.recommendation")
public class RecommendationProperties {

    /** Health score below this triggers critical-health recommendations (dashboard uses 40). */
    private double criticalHealthThreshold = 40.0;

    /** Risk score at or above this triggers elevated-risk recommendations. */
    private double highRiskThreshold = 70.0;

    /** Attention score at or above this triggers high-attention recommendations (dashboard uses 50). */
    private double highAttentionThreshold = 50.0;

    /** Minimum health point drop across trends to flag declining health. */
    private double healthDeclinePoints = 5.0;

    /** Completion percentage below this combined with high attention. */
    private double lowCompletionThreshold = 50.0;

    private int maxPerProject = 10;

    private int maxPerScope = 25;

    public double getCriticalHealthThreshold() {
        return criticalHealthThreshold;
    }

    public void setCriticalHealthThreshold(double criticalHealthThreshold) {
        this.criticalHealthThreshold = criticalHealthThreshold;
    }

    public double getHighRiskThreshold() {
        return highRiskThreshold;
    }

    public void setHighRiskThreshold(double highRiskThreshold) {
        this.highRiskThreshold = highRiskThreshold;
    }

    public double getHighAttentionThreshold() {
        return highAttentionThreshold;
    }

    public void setHighAttentionThreshold(double highAttentionThreshold) {
        this.highAttentionThreshold = highAttentionThreshold;
    }

    public double getHealthDeclinePoints() {
        return healthDeclinePoints;
    }

    public void setHealthDeclinePoints(double healthDeclinePoints) {
        this.healthDeclinePoints = healthDeclinePoints;
    }

    public double getLowCompletionThreshold() {
        return lowCompletionThreshold;
    }

    public void setLowCompletionThreshold(double lowCompletionThreshold) {
        this.lowCompletionThreshold = lowCompletionThreshold;
    }

    public int getMaxPerProject() {
        return maxPerProject;
    }

    public void setMaxPerProject(int maxPerProject) {
        this.maxPerProject = maxPerProject;
    }

    public int getMaxPerScope() {
        return maxPerScope;
    }

    public void setMaxPerScope(int maxPerScope) {
        this.maxPerScope = maxPerScope;
    }
}
