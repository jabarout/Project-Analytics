package com.projectanalytics.project.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProjectRepository extends JpaRepository<ProjectEntity, UUID> {

    Optional<ProjectEntity> findByWorkspaceIdAndOpenProjectId(UUID workspaceId, Long openProjectId);

    List<ProjectEntity> findByWorkspaceIdOrderByNameAsc(UUID workspaceId);

    long countByWorkspaceId(UUID workspaceId);

    @Query("""
            select p from ProjectEntity p
            join PortfolioProjectEntity pp on pp.projectId = p.id
            where pp.portfolioId = :portfolioId
            order by p.name asc
            """)
    List<ProjectEntity> findMembersByPortfolioIdOrderByNameAsc(@Param("portfolioId") UUID portfolioId);

    @Query("select count(pp) from PortfolioProjectEntity pp where pp.portfolioId = :portfolioId")
    long countMembersByPortfolioId(@Param("portfolioId") UUID portfolioId);

    @Query("""
            select count(p) from ProjectEntity p
            join PortfolioProjectEntity pp on pp.projectId = p.id
            where pp.portfolioId = :portfolioId
              and upper(p.status) = upper(:status)
            """)
    long countMembersByPortfolioIdAndStatusIgnoreCase(
            @Param("portfolioId") UUID portfolioId,
            @Param("status") String status
    );

    /** Non-archived members (status null or anything other than ARCHIVED). */
    @Query("""
            select count(p) from ProjectEntity p
            join PortfolioProjectEntity pp on pp.projectId = p.id
            where pp.portfolioId = :portfolioId
              and (p.status is null or upper(p.status) <> 'ARCHIVED')
            """)
    long countActiveMembersByPortfolioId(@Param("portfolioId") UUID portfolioId);

    @Query("""
            select coalesce(sum(p.budget), 0) from ProjectEntity p
            join PortfolioProjectEntity pp on pp.projectId = p.id
            where pp.portfolioId = :portfolioId
            """)
    BigDecimal sumBudgetByPortfolioId(@Param("portfolioId") UUID portfolioId);

    @Query("""
            select avg(p.progress) from ProjectEntity p
            join PortfolioProjectEntity pp on pp.projectId = p.id
            where pp.portfolioId = :portfolioId and p.progress is not null
            """)
    BigDecimal averageProgressByPortfolioId(@Param("portfolioId") UUID portfolioId);

    @Query("""
            select count(p) from ProjectEntity p
            join PortfolioProjectEntity pp on pp.projectId = p.id
            where pp.portfolioId = :portfolioId
              and p.endDate is not null
              and p.endDate < current_date
              and (p.status is null or upper(p.status) <> 'ARCHIVED')
            """)
    long countOverdueMembersByPortfolioId(@Param("portfolioId") UUID portfolioId);

    @Query("""
            select max(p.synchronizedAt) from ProjectEntity p
            join PortfolioProjectEntity pp on pp.projectId = p.id
            where pp.portfolioId = :portfolioId
            """)
    Instant maxSynchronizedAtByPortfolioId(@Param("portfolioId") UUID portfolioId);
}
