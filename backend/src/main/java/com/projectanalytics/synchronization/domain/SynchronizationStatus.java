package com.projectanalytics.synchronization.domain;

/**
 * Lifecycle status for a workspace or a synchronization run.
 */
public enum SynchronizationStatus {
    NEVER_RUN,
    RUNNING,
    SUCCESS,
    FAILED,
    PARTIAL
}
