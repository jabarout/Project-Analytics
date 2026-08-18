package com.projectanalytics.portfolio.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import java.util.List;
import java.util.UUID;

public interface PortfolioProjectRepository extends JpaRepository<
        PortfolioProjectEntity,
        PortfolioProjectEntity.PortfolioProjectId
        > {

    boolean existsByPortfolioIdAndProjectId(UUID portfolioId, UUID projectId);

    List<PortfolioProjectEntity> findByPortfolioId(UUID portfolioId);

    long countByPortfolioId(UUID portfolioId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    void deleteByPortfolioIdAndProjectId(UUID portfolioId, UUID projectId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    void deleteByPortfolioId(UUID portfolioId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    void deleteByProjectId(UUID projectId);
}
