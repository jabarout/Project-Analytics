package com.projectanalytics.synchronization.application;

import com.projectanalytics.common.exception.BusinessException;
import com.projectanalytics.common.exception.ErrorCode;
import com.projectanalytics.observability.PlatformMetrics;
import com.projectanalytics.synchronization.domain.SynchronizationStatus;
import com.projectanalytics.synchronization.domain.SynchronizationType;
import com.projectanalytics.synchronization.persistence.SynchronizationHistoryEntity;
import com.projectanalytics.synchronization.persistence.SynchronizationHistoryRepository;
import com.projectanalytics.synchronization.persistence.WorkspaceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Orchestrates OpenProject import. Does not calculate analytics.
 */
@Service
public class SynchronizationService {

    private static final Logger log = LoggerFactory.getLogger(SynchronizationService.class);

    private final WorkspaceRepository workspaceRepository;
    private final SynchronizationHistoryRepository historyRepository;
    private final SynchronizationHistoryService historyService;
    private final OperationalDataImportService operationalDataImportService;
    private final CacheInvalidationService cacheInvalidationService;
    private final List<PostSynchronizationHook> postSynchronizationHooks;
    private final PlatformMetrics platformMetrics;
    private final ConcurrentHashMap<UUID, Boolean> runningLocks = new ConcurrentHashMap<>();

    public SynchronizationService(
            WorkspaceRepository workspaceRepository,
            SynchronizationHistoryRepository historyRepository,
            SynchronizationHistoryService historyService,
            OperationalDataImportService operationalDataImportService,
            CacheInvalidationService cacheInvalidationService,
            List<PostSynchronizationHook> postSynchronizationHooks,
            PlatformMetrics platformMetrics
    ) {
        this.workspaceRepository = workspaceRepository;
        this.historyRepository = historyRepository;
        this.historyService = historyService;
        this.operationalDataImportService = operationalDataImportService;
        this.cacheInvalidationService = cacheInvalidationService;
        this.postSynchronizationHooks = postSynchronizationHooks;
        this.platformMetrics = platformMetrics;
    }

    public SynchronizationResult synchronizeWorkspace(UUID workspaceId, SynchronizationType requestedType) {
        if (runningLocks.putIfAbsent(workspaceId, Boolean.TRUE) != null
                || historyRepository.existsByWorkspaceIdAndStatus(workspaceId, SynchronizationStatus.RUNNING)) {
            throw new BusinessException(ErrorCode.SYNC_003);
        }

        if (!workspaceRepository.existsById(workspaceId)) {
            runningLocks.remove(workspaceId);
            throw new BusinessException(ErrorCode.WORKSPACE_001);
        }

        SynchronizationType storedType = resolveStoredType(workspaceId, requestedType);
        SynchronizationHistoryEntity history = historyService.startRun(workspaceId, storedType);
        long startedNanos = System.nanoTime();
        MDC.put("workspaceId", workspaceId.toString());

        try {
            OperationalDataImportService.ImportCounts counts =
                    operationalDataImportService.importOperationalData(workspaceId, storedType);
            SynchronizationHistoryEntity completed = historyService.completeSuccess(
                    history.getId(),
                    workspaceId,
                    counts.projects(),
                    counts.workPackages(),
                    counts.openProjectVersion()
            );

            cacheInvalidationService.invalidateWorkspaceCaches(workspaceId);
            postSynchronizationHooks.forEach(hook -> hook.onSynchronizationSucceeded(workspaceId));

            long durationMs = (System.nanoTime() - startedNanos) / 1_000_000L;
            platformMetrics.recordSyncSuccess(
                    storedType.name(),
                    durationMs,
                    counts.projects(),
                    counts.workPackages()
            );

            log.info(
                    "Synchronization SUCCESS workspace={} type={} projects={} workPackages={} durationMs={}",
                    workspaceId,
                    storedType,
                    counts.projects(),
                    counts.workPackages(),
                    durationMs
            );
            return toResult(completed, workspaceId);
        } catch (BusinessException exception) {
            long durationMs = (System.nanoTime() - startedNanos) / 1_000_000L;
            platformMetrics.recordSyncFailure(storedType.name(), durationMs);
            historyService.completeFailure(history.getId(), workspaceId, exception.getMessage());
            log.warn("Synchronization FAILED workspace={} reason={}", workspaceId, exception.getMessage());
            throw exception;
        } catch (RuntimeException exception) {
            long durationMs = (System.nanoTime() - startedNanos) / 1_000_000L;
            platformMetrics.recordSyncFailure(storedType.name(), durationMs);
            historyService.completeFailure(history.getId(), workspaceId, exception.getMessage());
            log.warn("Synchronization FAILED workspace={} reason={}", workspaceId, exception.getMessage());
            throw new BusinessException(ErrorCode.SYNC_001, ErrorCode.SYNC_001.getDefaultMessage(), exception);
        } finally {
            runningLocks.remove(workspaceId);
            MDC.remove("workspaceId");
        }
    }

    @Transactional(readOnly = true)
    public SynchronizationResult getLatestStatus(UUID workspaceId) {
        if (!workspaceRepository.existsById(workspaceId)) {
            throw new BusinessException(ErrorCode.WORKSPACE_001);
        }
        return historyRepository.findFirstByWorkspaceIdOrderByStartedAtDesc(workspaceId)
                .map(history -> toResult(history, workspaceId))
                .orElse(new SynchronizationResult(
                        null,
                        workspaceId,
                        null,
                        SynchronizationStatus.NEVER_RUN,
                        0,
                        0,
                        null,
                        null,
                        null,
                        null
                ));
    }

    private SynchronizationType resolveStoredType(UUID workspaceId, SynchronizationType requestedType) {
        boolean hasSuccess = historyRepository
                .findFirstByWorkspaceIdAndStatusOrderByFinishedAtDesc(workspaceId, SynchronizationStatus.SUCCESS)
                .isPresent();
        if (!hasSuccess) {
            return SynchronizationType.INITIAL;
        }
        return requestedType;
    }

    private static SynchronizationResult toResult(SynchronizationHistoryEntity history, UUID workspaceId) {
        return new SynchronizationResult(
                history.getId(),
                workspaceId,
                history.getSyncType(),
                history.getStatus(),
                history.getSynchronizedProjects(),
                history.getSynchronizedWorkPackages(),
                history.getStartedAt(),
                history.getFinishedAt(),
                history.getDurationMs(),
                history.getErrorMessage()
        );
    }
}
