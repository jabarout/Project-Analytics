package com.projectanalytics.analytics.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AnalyticsRepository extends JpaRepository<AnalyticsEntity, UUID> {

    Optional<AnalyticsEntity> findByProjectId(UUID projectId);

    void deleteByProjectId(UUID projectId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from AnalyticsEntity a where a.project.workspace.id = :workspaceId")
    void deleteByWorkspaceId(@Param("workspaceId") UUID workspaceId);

    List<AnalyticsEntity> findByProjectIdIn(Collection<UUID> projectIds);

    @Query("""
            select a from AnalyticsEntity a
            join fetch a.project p
            where p.workspace.id = :workspaceId
            """)
    List<AnalyticsEntity> findAllByWorkspaceId(@Param("workspaceId") UUID workspaceId);

    @Query("""
            select a from AnalyticsEntity a
            join fetch a.project p
            join PortfolioProjectEntity pp on pp.projectId = p.id
            where pp.portfolioId = :portfolioId
            """)
    List<AnalyticsEntity> findAllByPortfolioId(@Param("portfolioId") UUID portfolioId);

    @Query("""
            select avg(a.healthScore) from AnalyticsEntity a
            join a.project p
            where p.workspace.id = :workspaceId
            """)
    BigDecimal averageHealthByWorkspaceId(@Param("workspaceId") UUID workspaceId);

    @Query("""
            select avg(a.riskScore) from AnalyticsEntity a
            join a.project p
            where p.workspace.id = :workspaceId
            """)
    BigDecimal averageRiskByWorkspaceId(@Param("workspaceId") UUID workspaceId);

    @Query("""
            select avg(a.attentionScore) from AnalyticsEntity a
            join a.project p
            where p.workspace.id = :workspaceId
            """)
    BigDecimal averageAttentionByWorkspaceId(@Param("workspaceId") UUID workspaceId);

    @Query("""
            select avg(a.healthScore) from AnalyticsEntity a
            join a.project p
            join PortfolioProjectEntity pp on pp.projectId = p.id
            where pp.portfolioId = :portfolioId
            """)
    BigDecimal averageHealthByPortfolioId(@Param("portfolioId") UUID portfolioId);

    @Query("""
            select avg(a.attentionScore) from AnalyticsEntity a
            join a.project p
            join PortfolioProjectEntity pp on pp.projectId = p.id
            where pp.portfolioId = :portfolioId
            """)
    BigDecimal averageAttentionByPortfolioId(@Param("portfolioId") UUID portfolioId);
}
