package com.projectanalytics.project.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WorkPackageRepository extends JpaRepository<WorkPackageEntity, UUID> {

    Optional<WorkPackageEntity> findByProjectIdAndOpenProjectId(UUID projectId, Long openProjectId);

    long countByProjectWorkspaceId(UUID workspaceId);

    List<WorkPackageEntity> findByProjectId(UUID projectId);

    List<WorkPackageEntity> findByProjectIdIn(Collection<UUID> projectIds);

    void deleteByProjectId(UUID projectId);

    void deleteByProjectWorkspaceId(UUID workspaceId);

    @Query("""
            select count(wp) from WorkPackageEntity wp
            join PortfolioProjectEntity pp on pp.projectId = wp.project.id
            where pp.portfolioId = :portfolioId
              and wp.dueDate is not null
              and wp.dueDate < current_date
              and (wp.status is null or upper(wp.status) not in ('CLOSED', 'REJECTED'))
            """)
    long countOverdueByPortfolioId(@Param("portfolioId") UUID portfolioId);

    @Query("""
            select count(wp) from WorkPackageEntity wp
            join PortfolioProjectEntity pp on pp.projectId = wp.project.id
            where pp.portfolioId = :portfolioId
            """)
    long countByPortfolioId(@Param("portfolioId") UUID portfolioId);

    /**
     * Overdue open work packages per project in a workspace (local data only).
     * Returns [projectId, count] pairs.
     */
    @Query("""
            select wp.project.id, count(wp) from WorkPackageEntity wp
            where wp.project.workspace.id = :workspaceId
              and wp.dueDate is not null
              and wp.dueDate < current_date
              and (wp.status is null or upper(wp.status) not in (
                    'CLOSED', 'REJECTED', 'DONE', 'RESOLVED', 'COMPLETED'
              ))
            group by wp.project.id
            """)
    List<Object[]> countOverdueOpenByProjectInWorkspace(@Param("workspaceId") UUID workspaceId);
}
