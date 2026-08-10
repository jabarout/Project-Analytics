package com.projectanalytics.synchronization.api.dto;

import java.time.Instant;
import java.util.UUID;

public record WorkspaceResponse(
        UUID id,
        String name,
        String baseUrl,
        String version,
        String synchronizationStatus,
        Instant createdAt,
        Instant updatedAt
) {
}
