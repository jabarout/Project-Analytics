package com.projectanalytics.infrastructure.openproject;

import com.projectanalytics.infrastructure.openproject.dto.OpenProjectProjectDto;
import com.projectanalytics.infrastructure.openproject.dto.OpenProjectWorkPackageDto;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Port for OpenProject REST access. Implementations live in infrastructure.
 * Synchronization module depends on this abstraction only.
 */
public interface OpenProjectClient {

    /**
     * @param modifiedSince when non-null, only resources updated after this instant are returned (incremental).
     */
    List<OpenProjectProjectDto> fetchProjects(OpenProjectConnectionProperties connection, Instant modifiedSince);

    List<OpenProjectWorkPackageDto> fetchWorkPackages(
            OpenProjectConnectionProperties connection,
            long openProjectProjectId,
            Instant modifiedSince
    );

    /**
     * Project OpenProject id → display names of members with Project admin / Manager role.
     */
    Map<Long, List<String>> fetchProjectAdminNamesByProjectId(OpenProjectConnectionProperties connection);

    String fetchServerVersion(OpenProjectConnectionProperties connection);
}
