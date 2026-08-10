package com.projectanalytics.analytics.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface AnalyticsSnapshotRepository extends JpaRepository<AnalyticsSnapshotEntity, UUID> {

    List<AnalyticsSnapshotEntity> findTop20ByProjectIdOrderByCalculatedAtDesc(UUID projectId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from AnalyticsSnapshotEntity s where s.calculatedAt < :cutoff")
    int deleteByCalculatedAtBefore(@Param("cutoff") Instant cutoff);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from AnalyticsSnapshotEntity s where s.project.workspace.id = :workspaceId")
    void deleteByWorkspaceId(@Param("workspaceId") UUID workspaceId);
}
