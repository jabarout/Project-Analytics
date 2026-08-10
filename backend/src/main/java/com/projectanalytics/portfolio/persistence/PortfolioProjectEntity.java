package com.projectanalytics.portfolio.persistence;

import com.projectanalytics.project.persistence.ProjectEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Many-to-many membership: project appears in an analytical portfolio collection.
 * Does not represent ownership — workspace owns the project.
 */
@Entity
@Table(name = "portfolio_project")
@IdClass(PortfolioProjectEntity.PortfolioProjectId.class)
public class PortfolioProjectEntity {

    @Id
    @Column(name = "portfolio_id", nullable = false)
    private UUID portfolioId;

    @Id
    @Column(name = "project_id", nullable = false)
    private UUID projectId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "portfolio_id", insertable = false, updatable = false)
    private PortfolioEntity portfolio;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", insertable = false, updatable = false)
    private ProjectEntity project;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected PortfolioProjectEntity() {
    }

    public PortfolioProjectEntity(UUID portfolioId, UUID projectId) {
        this.portfolioId = portfolioId;
        this.projectId = projectId;
    }

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public UUID getPortfolioId() {
        return portfolioId;
    }

    public UUID getProjectId() {
        return projectId;
    }

    public ProjectEntity getProject() {
        return project;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public static final class PortfolioProjectId implements Serializable {
        private UUID portfolioId;
        private UUID projectId;

        public PortfolioProjectId() {
        }

        public PortfolioProjectId(UUID portfolioId, UUID projectId) {
            this.portfolioId = portfolioId;
            this.projectId = projectId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof PortfolioProjectId that)) {
                return false;
            }
            return Objects.equals(portfolioId, that.portfolioId)
                    && Objects.equals(projectId, that.projectId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(portfolioId, projectId);
        }
    }
}
