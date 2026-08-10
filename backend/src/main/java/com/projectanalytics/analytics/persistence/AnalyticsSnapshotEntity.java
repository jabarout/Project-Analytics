package com.projectanalytics.analytics.persistence;

import com.projectanalytics.common.persistence.BaseEntity;
import com.projectanalytics.project.persistence.ProjectEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "analytics_snapshot")
public class AnalyticsSnapshotEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
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

    @Column(name = "calculated_at", nullable = false)
    private Instant calculatedAt;

    protected AnalyticsSnapshotEntity() {
    }

    public AnalyticsSnapshotEntity(ProjectEntity project) {
        this.project = project;
    }

    public ProjectEntity getProject() {
        return project;
    }

    public void setHealthScore(BigDecimal healthScore) {
        this.healthScore = healthScore;
    }

    public void setRiskScore(BigDecimal riskScore) {
        this.riskScore = riskScore;
    }

    public void setAttentionScore(BigDecimal attentionScore) {
        this.attentionScore = attentionScore;
    }

    public void setCompletionPercentage(BigDecimal completionPercentage) {
        this.completionPercentage = completionPercentage;
    }

    public void setExpectedProgress(BigDecimal expectedProgress) {
        this.expectedProgress = expectedProgress;
    }

    public void setProgressGap(BigDecimal progressGap) {
        this.progressGap = progressGap;
    }

    public void setOverdueRatio(BigDecimal overdueRatio) {
        this.overdueRatio = overdueRatio;
    }

    public void setCalculatedAt(Instant calculatedAt) {
        this.calculatedAt = calculatedAt;
    }

    public BigDecimal getHealthScore() {
        return healthScore;
    }

    public BigDecimal getRiskScore() {
        return riskScore;
    }

    public BigDecimal getAttentionScore() {
        return attentionScore;
    }

    public BigDecimal getCompletionPercentage() {
        return completionPercentage;
    }

    public BigDecimal getExpectedProgress() {
        return expectedProgress;
    }

    public BigDecimal getProgressGap() {
        return progressGap;
    }

    public BigDecimal getOverdueRatio() {
        return overdueRatio;
    }

    public Instant getCalculatedAt() {
        return calculatedAt;
    }
}
