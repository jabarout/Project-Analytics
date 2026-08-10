package com.projectanalytics.recommendation.persistence;

import com.projectanalytics.common.persistence.BaseEntity;
import com.projectanalytics.recommendation.domain.RecommendationSeverity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "recommendation")
public class RecommendationEntity extends BaseEntity {

    @Column(name = "analytics_id", nullable = false)
    private UUID analyticsId;

    @Column(name = "project_id", nullable = false)
    private UUID projectId;

    @Column(name = "rule_code", nullable = false, length = 80)
    private String ruleCode;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "description", nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false, length = 30)
    private RecommendationSeverity severity;

    @Column(name = "explanation", nullable = false)
    private String explanation;

    @Column(name = "suggested_action")
    private String suggestedAction;

    @Column(name = "priority_rank", nullable = false)
    private int priorityRank;

    @Column(name = "supporting_metrics")
    private String supportingMetrics;

    @Column(name = "generated_at", nullable = false)
    private Instant generatedAt;

    protected RecommendationEntity() {
    }

    public RecommendationEntity(
            UUID analyticsId,
            UUID projectId,
            String ruleCode,
            String title,
            String description,
            RecommendationSeverity severity,
            String explanation,
            String suggestedAction,
            int priorityRank,
            String supportingMetrics,
            Instant generatedAt
    ) {
        this.analyticsId = analyticsId;
        this.projectId = projectId;
        this.ruleCode = ruleCode;
        this.title = title;
        this.description = description;
        this.severity = severity;
        this.explanation = explanation;
        this.suggestedAction = suggestedAction;
        this.priorityRank = priorityRank;
        this.supportingMetrics = supportingMetrics;
        this.generatedAt = generatedAt;
    }

    public UUID getAnalyticsId() {
        return analyticsId;
    }

    public UUID getProjectId() {
        return projectId;
    }

    public String getRuleCode() {
        return ruleCode;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public RecommendationSeverity getSeverity() {
        return severity;
    }

    public String getExplanation() {
        return explanation;
    }

    public String getSuggestedAction() {
        return suggestedAction;
    }

    public int getPriorityRank() {
        return priorityRank;
    }

    public String getSupportingMetrics() {
        return supportingMetrics;
    }

    public Instant getGeneratedAt() {
        return generatedAt;
    }
}
