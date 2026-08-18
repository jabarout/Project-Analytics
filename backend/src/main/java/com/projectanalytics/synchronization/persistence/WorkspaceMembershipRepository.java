package com.projectanalytics.synchronization.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WorkspaceMembershipRepository extends JpaRepository<WorkspaceMembershipEntity, UUID> {

    Optional<WorkspaceMembershipEntity> findByWorkspaceIdAndUserId(UUID workspaceId, UUID userId);

    List<WorkspaceMembershipEntity> findByWorkspaceIdOrderByCreatedAtAsc(UUID workspaceId);

    List<WorkspaceMembershipEntity> findByUserIdOrderByCreatedAtAsc(UUID userId);

    List<WorkspaceMembershipEntity> findByUserIdAndAnalyticsAccessTrue(UUID userId);

    boolean existsByWorkspaceIdAndUserIdAndAnalyticsAccessTrue(UUID workspaceId, UUID userId);

    boolean existsByWorkspaceIdAndUserIdAndWorkspaceAdminTrue(UUID workspaceId, UUID userId);

    long countByWorkspaceIdAndWorkspaceAdminTrue(UUID workspaceId);
}
