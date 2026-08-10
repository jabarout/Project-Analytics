package com.projectanalytics.synchronization.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface WorkspaceRepository extends JpaRepository<WorkspaceEntity, UUID> {

    boolean existsByBaseUrlIgnoreCase(String baseUrl);

    Optional<WorkspaceEntity> findByBaseUrlIgnoreCase(String baseUrl);
}
