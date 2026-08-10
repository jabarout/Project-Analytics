package com.projectanalytics.analytics.application;

import com.projectanalytics.synchronization.application.PostSynchronizationHook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Recalculates analytics after successful synchronization using local data only.
 */
@Component
@Primary
@Order(100)
public class AnalyticsRecalculationHook implements PostSynchronizationHook {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsRecalculationHook.class);

    private final AnalyticsRecalculationService recalculationService;

    public AnalyticsRecalculationHook(AnalyticsRecalculationService recalculationService) {
        this.recalculationService = recalculationService;
    }

    @Override
    public void onSynchronizationSucceeded(UUID workspaceId) {
        try {
            int count = recalculationService.recalculateWorkspace(workspaceId);
            log.info("Post-sync analytics completed for workspace {} ({} projects)", workspaceId, count);
        } catch (RuntimeException exception) {
            log.error("Post-sync analytics failed for workspace {}: {}", workspaceId, exception.getMessage());
            // Do not fail synchronization success path; analytics can be recomputed later.
        }
    }
}
