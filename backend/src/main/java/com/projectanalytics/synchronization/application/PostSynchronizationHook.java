package com.projectanalytics.synchronization.application;

import java.util.UUID;

/**
 * Extension point invoked after a successful synchronization.
 * Analytics recalculation (M5) will implement this without changing the sync module.
 */
public interface PostSynchronizationHook {

    void onSynchronizationSucceeded(UUID workspaceId);
}
