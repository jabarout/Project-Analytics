package com.projectanalytics.synchronization.application;

import com.projectanalytics.common.exception.BusinessException;
import com.projectanalytics.common.exception.ErrorCode;
import com.projectanalytics.synchronization.domain.SynchronizationStatus;
import com.projectanalytics.synchronization.domain.SynchronizationType;
import com.projectanalytics.synchronization.persistence.SynchronizationHistoryEntity;
import com.projectanalytics.synchronization.persistence.SynchronizationHistoryRepository;
import com.projectanalytics.synchronization.persistence.WorkspaceEntity;
import com.projectanalytics.synchronization.persistence.WorkspaceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

/**
 * Persists synchronization history in independent transactions so audit rows survive import rollbacks.
 */
@Service
public class SynchronizationHistoryService {

    private final WorkspaceRepository workspaceRepository;
    private final SynchronizationHistoryRepository historyRepository;

    public SynchronizationHistoryService(
            WorkspaceRepository workspaceRepository,
            SynchronizationHistoryRepository historyRepository
    ) {
        this.workspaceRepository = workspaceRepository;
        this.historyRepository = historyRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public SynchronizationHistoryEntity startRun(UUID workspaceId, SynchronizationType syncType) {
        WorkspaceEntity workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new BusinessException(ErrorCode.WORKSPACE_001));
        Instant startedAt = Instant.now();
        SynchronizationHistoryEntity history = historyRepository.save(
                new SynchronizationHistoryEntity(
                        workspace,
                        startedAt,
                        SynchronizationStatus.RUNNING,
                        syncType
                )
        );
        workspace.setSynchronizationStatus(SynchronizationStatus.RUNNING);
        workspaceRepository.save(workspace);
        return history;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public SynchronizationHistoryEntity completeSuccess(
            UUID historyId,
            UUID workspaceId,
            int projectCount,
            int workPackageCount,
            String openProjectVersion
    ) {
        SynchronizationHistoryEntity history = historyRepository.findById(historyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SYNC_001));
        WorkspaceEntity workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new BusinessException(ErrorCode.WORKSPACE_001));

        Instant finishedAt = Instant.now();
        history.setStatus(SynchronizationStatus.SUCCESS);
        history.setSynchronizedProjects(projectCount);
        history.setSynchronizedWorkPackages(workPackageCount);
        history.setFinishedAt(finishedAt);
        history.setDurationMs(Duration.between(history.getStartedAt(), finishedAt).toMillis());
        historyRepository.save(history);

        workspace.setSynchronizationStatus(SynchronizationStatus.SUCCESS);
        if (openProjectVersion != null && !openProjectVersion.isBlank()) {
            workspace.setVersion(openProjectVersion);
        }
        workspaceRepository.save(workspace);
        return history;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public SynchronizationHistoryEntity completeFailure(UUID historyId, UUID workspaceId, String message) {
        SynchronizationHistoryEntity history = historyRepository.findById(historyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SYNC_001));
        WorkspaceEntity workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new BusinessException(ErrorCode.WORKSPACE_001));

        Instant finishedAt = Instant.now();
        history.setStatus(SynchronizationStatus.FAILED);
        history.setFinishedAt(finishedAt);
        history.setDurationMs(Duration.between(history.getStartedAt(), finishedAt).toMillis());
        history.setErrorMessage(truncate(message, 2000));
        historyRepository.save(history);

        workspace.setSynchronizationStatus(SynchronizationStatus.FAILED);
        workspaceRepository.save(workspace);
        return history;
    }

    private static String truncate(String value, int max) {
        if (value == null) {
            return null;
        }
        return value.length() <= max ? value : value.substring(0, max);
    }
}
