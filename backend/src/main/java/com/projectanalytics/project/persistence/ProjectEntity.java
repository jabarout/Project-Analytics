package com.projectanalytics.project.persistence;

import com.projectanalytics.common.persistence.BaseEntity;
import com.projectanalytics.synchronization.persistence.WorkspaceEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/**
 * Synchronized OpenProject project (analytical copy).
 * Owned by a workspace; optional portfolio membership is many-to-many via portfolio_project.
 */
@Entity
@Table(name = "project")
public class ProjectEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "workspace_id", nullable = false)
    private WorkspaceEntity workspace;

    @Column(name = "openproject_id", nullable = false)
    private Long openProjectId;

    @Column(name = "name", nullable = false, length = 500)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "status", length = 100)
    private String status;

    @Column(name = "budget", precision = 19, scale = 4)
    private BigDecimal budget;

    @Column(name = "progress", precision = 5, scale = 2)
    private BigDecimal progress;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    /** OpenProject project admin display name(s), synced from memberships. */
    @Column(name = "admin_name", length = 500)
    private String adminName;

    @Column(name = "synchronized_at")
    private Instant synchronizedAt;

    protected ProjectEntity() {
    }

    public ProjectEntity(WorkspaceEntity workspace, Long openProjectId, String name) {
        this.workspace = workspace;
        this.openProjectId = openProjectId;
        this.name = name;
    }

    public WorkspaceEntity getWorkspace() {
        return workspace;
    }

    public void setWorkspace(WorkspaceEntity workspace) {
        this.workspace = workspace;
    }

    public Long getOpenProjectId() {
        return openProjectId;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public BigDecimal getBudget() {
        return budget;
    }

    public void setBudget(BigDecimal budget) {
        this.budget = budget;
    }

    public BigDecimal getProgress() {
        return progress;
    }

    public void setProgress(BigDecimal progress) {
        this.progress = progress;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getAdminName() {
        return adminName;
    }

    public void setAdminName(String adminName) {
        this.adminName = adminName;
    }

    public Instant getSynchronizedAt() {
        return synchronizedAt;
    }

    public void setSynchronizedAt(Instant synchronizedAt) {
        this.synchronizedAt = synchronizedAt;
    }
}
