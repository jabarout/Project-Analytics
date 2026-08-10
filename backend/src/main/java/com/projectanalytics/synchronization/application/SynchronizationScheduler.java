package com.projectanalytics.synchronization.application;

import com.projectanalytics.synchronization.domain.SynchronizationType;
import com.projectanalytics.synchronization.persistence.WorkspaceEntity;
import com.projectanalytics.synchronization.persistence.WorkspaceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduled synchronization driver. Interval is externalized via configuration.
 */
@Component
@ConditionalOnProperty(name = "projectanalytics.sync.enabled", havingValue = "true", matchIfMissing = true)
public class SynchronizationScheduler {

    private static final Logger log = LoggerFactory.getLogger(SynchronizationScheduler.class);

    private final WorkspaceRepository workspaceRepository;
    private final SynchronizationService synchronizationService;
    private final SynchronizationProperties synchronizationProperties;

    public SynchronizationScheduler(
            WorkspaceRepository workspaceRepository,
            SynchronizationService synchronizationService,
            SynchronizationProperties synchronizationProperties
    ) {
        this.workspaceRepository = workspaceRepository;
        this.synchronizationService = synchronizationService;
        this.synchronizationProperties = synchronizationProperties;
    }

    @Scheduled(fixedDelayString = "${projectanalytics.sync.interval-ms:3600000}")
    public void runScheduledSynchronization() {
        if (!synchronizationProperties.isEnabled()) {
            return;
        }
        for (WorkspaceEntity workspace : workspaceRepository.findAll()) {
            try {
                log.info("Starting scheduled synchronization for workspace {}", workspace.getId());
                synchronizationService.synchronizeWorkspace(workspace.getId(), SynchronizationType.SCHEDULED);
            } catch (RuntimeException exception) {
                log.warn(
                        "Scheduled synchronization failed for workspace {}: {}",
                        workspace.getId(),
                        exception.getMessage()
                );
            }
        }
    }
}
