package com.projectanalytics.synchronization.application;

import com.projectanalytics.synchronization.domain.SynchronizationStatus;
import com.projectanalytics.synchronization.domain.SynchronizationType;

import java.time.Instant;
import java.util.UUID;

public record SynchronizationResult(
        UUID historyId,
        UUID workspaceId,
        SynchronizationType syncType,
        SynchronizationStatus status,
        int synchronizedProjects,
        int synchronizedWorkPackages,
        Instant startedAt,
        Instant finishedAt,
        Long durationMs,
        String errorMessage
) {
}
