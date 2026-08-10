package com.projectanalytics.reporting.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Externalized reporting storage configuration (docs/17_Configuration.md §12).
 */
@ConfigurationProperties(prefix = "projectanalytics.reporting")
public class ReportingProperties {

    /**
     * Directory where generated report files are stored (outside the application binary).
     */
    private String storagePath = "./data/reports";

    /**
     * Delete report metadata and files older than this many days (when purge enabled).
     */
    private int retentionDays = 90;

    /**
     * When true, scheduled purge removes reports older than {@link #retentionDays}.
     */
    private boolean purgeEnabled = true;

    /**
     * Soft max size in bytes for generated files (validation / future enforcement).
     */
    private long maxSizeBytes = 10_485_760L;

    public String getStoragePath() {
        return storagePath;
    }

    public void setStoragePath(String storagePath) {
        this.storagePath = storagePath;
    }

    public int getRetentionDays() {
        return retentionDays;
    }

    public void setRetentionDays(int retentionDays) {
        this.retentionDays = retentionDays;
    }

    public boolean isPurgeEnabled() {
        return purgeEnabled;
    }

    public void setPurgeEnabled(boolean purgeEnabled) {
        this.purgeEnabled = purgeEnabled;
    }

    public long getMaxSizeBytes() {
        return maxSizeBytes;
    }

    public void setMaxSizeBytes(long maxSizeBytes) {
        this.maxSizeBytes = maxSizeBytes;
    }
}
