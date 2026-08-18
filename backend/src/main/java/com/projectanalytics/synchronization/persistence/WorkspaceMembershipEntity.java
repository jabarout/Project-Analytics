package com.projectanalytics.synchronization.persistence;

import com.projectanalytics.common.persistence.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "workspace_membership")
public class WorkspaceMembershipEntity extends BaseEntity {

    @Column(name = "workspace_id", nullable = false)
    private UUID workspaceId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "workspace_admin", nullable = false)
    private boolean workspaceAdmin;

    @Column(name = "analytics_access", nullable = false)
    private boolean analyticsAccess;

    protected WorkspaceMembershipEntity() {
    }

    public WorkspaceMembershipEntity(UUID workspaceId, UUID userId, boolean workspaceAdmin, boolean analyticsAccess) {
        this.workspaceId = workspaceId;
        this.userId = userId;
        this.workspaceAdmin = workspaceAdmin;
        this.analyticsAccess = analyticsAccess;
    }

    public UUID getWorkspaceId() {
        return workspaceId;
    }

    public UUID getUserId() {
        return userId;
    }

    public boolean isWorkspaceAdmin() {
        return workspaceAdmin;
    }

    public void setWorkspaceAdmin(boolean workspaceAdmin) {
        this.workspaceAdmin = workspaceAdmin;
    }

    public boolean isAnalyticsAccess() {
        return analyticsAccess;
    }

    public void setAnalyticsAccess(boolean analyticsAccess) {
        this.analyticsAccess = analyticsAccess;
    }
}
