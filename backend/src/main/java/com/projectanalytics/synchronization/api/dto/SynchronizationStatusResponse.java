package com.projectanalytics.synchronization.api.dto;

import java.time.Instant;
import java.util.UUID;

public record SynchronizationStatusResponse(
        UUID historyId,
        UUID workspaceId,
        String syncType,
        String status,
        int synchronizedProjects,
        int synchronizedWorkPackages,
        Instant startedAt,
        Instant finishedAt,
        Long durationMs,
        String errorMessage
) {
}
