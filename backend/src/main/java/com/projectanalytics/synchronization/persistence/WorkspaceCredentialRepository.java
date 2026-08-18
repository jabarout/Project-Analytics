package com.projectanalytics.synchronization.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface WorkspaceCredentialRepository extends JpaRepository<WorkspaceCredentialEntity, UUID> {

    Optional<WorkspaceCredentialEntity> findByWorkspaceId(UUID workspaceId);

    boolean existsByWorkspaceId(UUID workspaceId);
}
