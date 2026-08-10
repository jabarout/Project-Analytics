package com.projectanalytics.project.persistence;

import com.projectanalytics.common.persistence.BaseEntity;
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
 * Synchronized OpenProject work package (analytical copy).
 */
@Entity
@Table(name = "work_package")
public class WorkPackageEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private ProjectEntity project;

    @Column(name = "openproject_id", nullable = false)
    private Long openProjectId;

    @Column(name = "subject", nullable = false, length = 1000)
    private String subject;

    @Column(name = "type", length = 100)
    private String type;

    @Column(name = "status", length = 100)
    private String status;

    @Column(name = "priority", length = 100)
    private String priority;

    @Column(name = "assignee", length = 255)
    private String assignee;

    @Column(name = "estimated_hours", precision = 12, scale = 2)
    private BigDecimal estimatedHours;

    @Column(name = "spent_hours", precision = 12, scale = 2)
    private BigDecimal spentHours;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "synchronized_at")
    private Instant synchronizedAt;

    protected WorkPackageEntity() {
    }

    public WorkPackageEntity(ProjectEntity project, Long openProjectId, String subject) {
        this.project = project;
        this.openProjectId = openProjectId;
        this.subject = subject;
    }

    public ProjectEntity getProject() {
        return project;
    }

    public Long getOpenProjectId() {
        return openProjectId;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getAssignee() {
        return assignee;
    }

    public void setAssignee(String assignee) {
        this.assignee = assignee;
    }

    public BigDecimal getEstimatedHours() {
        return estimatedHours;
    }

    public void setEstimatedHours(BigDecimal estimatedHours) {
        this.estimatedHours = estimatedHours;
    }

    public BigDecimal getSpentHours() {
        return spentHours;
    }

    public void setSpentHours(BigDecimal spentHours) {
        this.spentHours = spentHours;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public Instant getSynchronizedAt() {
        return synchronizedAt;
    }

    public void setSynchronizedAt(Instant synchronizedAt) {
        this.synchronizedAt = synchronizedAt;
    }
}
