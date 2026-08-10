package com.projectanalytics.synchronization.persistence;

import com.projectanalytics.common.persistence.BaseEntity;
import com.projectanalytics.synchronization.domain.SynchronizationStatus;
import com.projectanalytics.synchronization.domain.SynchronizationType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "synchronization_history")
public class SynchronizationHistoryEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "workspace_id", nullable = false)
    private WorkspaceEntity workspace;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "finished_at")
    private Instant finishedAt;

    @Column(name = "duration_ms")
    private Long durationMs;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private SynchronizationStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "sync_type", nullable = false, length = 50)
    private SynchronizationType syncType;

    @Column(name = "synchronized_projects", nullable = false)
    private int synchronizedProjects;

    @Column(name = "synchronized_work_packages", nullable = false)
    private int synchronizedWorkPackages;

    @Column(name = "error_message")
    private String errorMessage;

    protected SynchronizationHistoryEntity() {
    }

    public SynchronizationHistoryEntity(
            WorkspaceEntity workspace,
            Instant startedAt,
            SynchronizationStatus status,
            SynchronizationType syncType
    ) {
        this.workspace = workspace;
        this.startedAt = startedAt;
        this.status = status;
        this.syncType = syncType;
        this.synchronizedProjects = 0;
        this.synchronizedWorkPackages = 0;
    }

    public WorkspaceEntity getWorkspace() {
        return workspace;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public Instant getFinishedAt() {
        return finishedAt;
    }

    public void setFinishedAt(Instant finishedAt) {
        this.finishedAt = finishedAt;
    }

    public Long getDurationMs() {
        return durationMs;
    }

    public void setDurationMs(Long durationMs) {
        this.durationMs = durationMs;
    }

    public SynchronizationStatus getStatus() {
        return status;
    }

    public void setStatus(SynchronizationStatus status) {
        this.status = status;
    }

    public SynchronizationType getSyncType() {
        return syncType;
    }

    public int getSynchronizedProjects() {
        return synchronizedProjects;
    }

    public void setSynchronizedProjects(int synchronizedProjects) {
        this.synchronizedProjects = synchronizedProjects;
    }

    public int getSynchronizedWorkPackages() {
        return synchronizedWorkPackages;
    }

    public void setSynchronizedWorkPackages(int synchronizedWorkPackages) {
        this.synchronizedWorkPackages = synchronizedWorkPackages;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
