package com.projectanalytics.synchronization.persistence;

import com.projectanalytics.common.persistence.BaseEntity;
import com.projectanalytics.synchronization.domain.SynchronizationStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

@Entity
@Table(name = "workspace")
public class WorkspaceEntity extends BaseEntity {

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "base_url", nullable = false, length = 500, unique = true)
    private String baseUrl;

    @Column(name = "version", length = 50)
    private String version;

    @Enumerated(EnumType.STRING)
    @Column(name = "synchronization_status", nullable = false, length = 50)
    private SynchronizationStatus synchronizationStatus = SynchronizationStatus.NEVER_RUN;

    protected WorkspaceEntity() {
    }

    public WorkspaceEntity(String name, String baseUrl) {
        this.name = name;
        this.baseUrl = baseUrl;
        this.synchronizationStatus = SynchronizationStatus.NEVER_RUN;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public SynchronizationStatus getSynchronizationStatus() {
        return synchronizationStatus;
    }

    public void setSynchronizationStatus(SynchronizationStatus synchronizationStatus) {
        this.synchronizationStatus = synchronizationStatus;
    }
}
