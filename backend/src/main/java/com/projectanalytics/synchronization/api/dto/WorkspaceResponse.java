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
        Instant updatedAt,
        /** True when the current caller is Workspace Admin for this connection. */
        boolean workspaceAdmin,
        /** True when the current caller has analytics access (always true for list results). */
        boolean analyticsAccess
) {
}
