package com.projectanalytics.synchronization.persistence;

import com.projectanalytics.synchronization.domain.SynchronizationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SynchronizationHistoryRepository extends JpaRepository<SynchronizationHistoryEntity, UUID> {

    void deleteByWorkspaceId(UUID workspaceId);

    boolean existsByWorkspaceIdAndStatus(UUID workspaceId, SynchronizationStatus status);

    Optional<SynchronizationHistoryEntity> findFirstByWorkspaceIdOrderByStartedAtDesc(UUID workspaceId);

    Optional<SynchronizationHistoryEntity> findFirstByWorkspaceIdAndStatusOrderByFinishedAtDesc(
            UUID workspaceId,
            SynchronizationStatus status
    );

    List<SynchronizationHistoryEntity> findTop20ByWorkspaceIdOrderByStartedAtDesc(UUID workspaceId);

    /** Latest successful synchronization across all workspaces (for ops staleness gauge). */
    Optional<SynchronizationHistoryEntity> findFirstByStatusOrderByFinishedAtDesc(SynchronizationStatus status);
}
