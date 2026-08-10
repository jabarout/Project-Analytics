package com.projectanalytics.synchronization.application;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Externalized synchronization settings (Configuration documentation).
 */
@ConfigurationProperties(prefix = "projectanalytics.sync")
public class SynchronizationProperties {

    private boolean enabled = true;

    /**
     * Scheduled sync interval in milliseconds.
     */
    private long intervalMs = 3_600_000L;

    private int batchSize = 100;

    private int retryCount = 1;

    private int timeoutSeconds = 120;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public long getIntervalMs() {
        return intervalMs;
    }

    public void setIntervalMs(long intervalMs) {
        this.intervalMs = intervalMs;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }

    public int getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public void setTimeoutSeconds(int timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }
}
