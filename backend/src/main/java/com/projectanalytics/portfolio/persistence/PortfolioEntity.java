package com.projectanalytics.portfolio.persistence;

import com.projectanalytics.common.persistence.BaseEntity;
import com.projectanalytics.synchronization.persistence.WorkspaceEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;

/**
 * Logical collection of projects within a workspace.
 * Operational project data is populated by synchronization; portfolio management is local-only.
 */
@Entity
@Table(name = "portfolio")
public class PortfolioEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "workspace_id", nullable = false)
    private WorkspaceEntity workspace;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "health_score", precision = 5, scale = 2)
    private BigDecimal healthScore;

    @Column(name = "attention_score", precision = 5, scale = 2)
    private BigDecimal attentionScore;

    protected PortfolioEntity() {
    }

    public PortfolioEntity(WorkspaceEntity workspace, String name, String description) {
        this.workspace = workspace;
        this.name = name;
        this.description = description;
    }

    public WorkspaceEntity getWorkspace() {
        return workspace;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getHealthScore() {
        return healthScore;
    }

    public void setHealthScore(BigDecimal healthScore) {
        this.healthScore = healthScore;
    }

    public BigDecimal getAttentionScore() {
        return attentionScore;
    }

    public void setAttentionScore(BigDecimal attentionScore) {
        this.attentionScore = attentionScore;
    }
}
