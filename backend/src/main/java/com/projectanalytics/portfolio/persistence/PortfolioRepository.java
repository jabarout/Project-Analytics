package com.projectanalytics.portfolio.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PortfolioRepository extends JpaRepository<PortfolioEntity, UUID> {

    List<PortfolioEntity> findByWorkspaceIdOrderByNameAsc(UUID workspaceId);

    List<PortfolioEntity> findAllByOrderByNameAsc();

    Optional<PortfolioEntity> findByWorkspaceIdAndName(UUID workspaceId, String name);

    boolean existsByWorkspaceIdAndNameIgnoreCase(UUID workspaceId, String name);

    boolean existsByWorkspaceIdAndNameIgnoreCaseAndIdNot(UUID workspaceId, String name, UUID id);
}
