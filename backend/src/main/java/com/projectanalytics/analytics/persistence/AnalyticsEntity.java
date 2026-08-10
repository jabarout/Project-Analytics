package com.projectanalytics.analytics.persistence;

import com.projectanalytics.common.persistence.BaseEntity;
import com.projectanalytics.project.persistence.ProjectEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "analytics")
public class AnalyticsEntity extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false, unique = true)
    private ProjectEntity project;

    @Column(name = "health_score", nullable = false, precision = 5, scale = 2)
    private BigDecimal healthScore;

    @Column(name = "risk_score", nullable = false, precision = 5, scale = 2)
    private BigDecimal riskScore;

    @Column(name = "attention_score", nullable = false, precision = 5, scale = 2)
    private BigDecimal attentionScore;

    @Column(name = "completion_percentage", precision = 5, scale = 2)
    private BigDecimal completionPercentage;

    @Column(name = "expected_progress", precision = 5, scale = 2)
    private BigDecimal expectedProgress;

    @Column(name = "progress_gap", precision = 5, scale = 2)
    private BigDecimal progressGap;

    @Column(name = "overdue_ratio", precision = 5, scale = 4)
    private BigDecimal overdueRatio;

    @Column(name = "avg_overdue_age_days", precision = 8, scale = 2)
    private BigDecimal avgOverdueAgeDays;

    @Column(name = "max_overdue_age_days")
    private Integer maxOverdueAgeDays;

    @Column(name = "budget_variance", precision = 12, scale = 4)
    private BigDecimal budgetVariance;

    @Column(name = "schedule_variance", precision = 12, scale = 4)
    private BigDecimal scheduleVariance;

    @Column(name = "health_status", length = 50)
    private String healthStatus;

    @Column(name = "health_explanation")
    private String healthExplanation;

    @Column(name = "risk_level", length = 50)
    private String riskLevel;

    @Column(name = "risk_explanation")
    private String riskExplanation;

    @Column(name = "attention_explanation")
    private String attentionExplanation;

    @Column(name = "health_factors_json")
    private String healthFactorsJson;

    @Column(name = "risk_factors_json")
    private String riskFactorsJson;

    @Column(name = "attention_factors_json")
    private String attentionFactorsJson;

    @Column(name = "calculated_at", nullable = false)
    private Instant calculatedAt;

    protected AnalyticsEntity() {
    }

    public AnalyticsEntity(ProjectEntity project) {
        this.project = project;
    }

    public ProjectEntity getProject() {
        return project;
    }

    public BigDecimal getHealthScore() {
        return healthScore;
    }

    public void setHealthScore(BigDecimal healthScore) {
        this.healthScore = healthScore;
    }

    public BigDecimal getRiskScore() {
        return riskScore;
    }

    public void setRiskScore(BigDecimal riskScore) {
        this.riskScore = riskScore;
    }

    public BigDecimal getAttentionScore() {
        return attentionScore;
    }

    public void setAttentionScore(BigDecimal attentionScore) {
        this.attentionScore = attentionScore;
    }

    public BigDecimal getCompletionPercentage() {
        return completionPercentage;
    }

    public void setCompletionPercentage(BigDecimal completionPercentage) {
        this.completionPercentage = completionPercentage;
    }

    public BigDecimal getExpectedProgress() {
        return expectedProgress;
    }

    public void setExpectedProgress(BigDecimal expectedProgress) {
        this.expectedProgress = expectedProgress;
    }

    public BigDecimal getProgressGap() {
        return progressGap;
    }

    public void setProgressGap(BigDecimal progressGap) {
        this.progressGap = progressGap;
    }

    public BigDecimal getOverdueRatio() {
        return overdueRatio;
    }

    public void setOverdueRatio(BigDecimal overdueRatio) {
        this.overdueRatio = overdueRatio;
    }

    public BigDecimal getAvgOverdueAgeDays() {
        return avgOverdueAgeDays;
    }

    public void setAvgOverdueAgeDays(BigDecimal avgOverdueAgeDays) {
        this.avgOverdueAgeDays = avgOverdueAgeDays;
    }

    public Integer getMaxOverdueAgeDays() {
        return maxOverdueAgeDays;
    }

    public void setMaxOverdueAgeDays(Integer maxOverdueAgeDays) {
        this.maxOverdueAgeDays = maxOverdueAgeDays;
    }

    public String getHealthFactorsJson() {
        return healthFactorsJson;
    }

    public void setHealthFactorsJson(String healthFactorsJson) {
        this.healthFactorsJson = healthFactorsJson;
    }

    public String getRiskFactorsJson() {
        return riskFactorsJson;
    }

    public void setRiskFactorsJson(String riskFactorsJson) {
        this.riskFactorsJson = riskFactorsJson;
    }

    public String getAttentionFactorsJson() {
        return attentionFactorsJson;
    }

    public void setAttentionFactorsJson(String attentionFactorsJson) {
        this.attentionFactorsJson = attentionFactorsJson;
    }

    public BigDecimal getBudgetVariance() {
        return budgetVariance;
    }

    public void setBudgetVariance(BigDecimal budgetVariance) {
        this.budgetVariance = budgetVariance;
    }

    public BigDecimal getScheduleVariance() {
        return scheduleVariance;
    }

    public void setScheduleVariance(BigDecimal scheduleVariance) {
        this.scheduleVariance = scheduleVariance;
    }

    public String getHealthStatus() {
        return healthStatus;
    }

    public void setHealthStatus(String healthStatus) {
        this.healthStatus = healthStatus;
    }

    public String getHealthExplanation() {
        return healthExplanation;
    }

    public void setHealthExplanation(String healthExplanation) {
        this.healthExplanation = healthExplanation;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }

    public String getRiskExplanation() {
        return riskExplanation;
    }

    public void setRiskExplanation(String riskExplanation) {
        this.riskExplanation = riskExplanation;
    }

    public String getAttentionExplanation() {
        return attentionExplanation;
    }

    public void setAttentionExplanation(String attentionExplanation) {
        this.attentionExplanation = attentionExplanation;
    }

    public Instant getCalculatedAt() {
        return calculatedAt;
    }

    public void setCalculatedAt(Instant calculatedAt) {
        this.calculatedAt = calculatedAt;
    }
}
