package com.projectanalytics.reporting.application;

import com.projectanalytics.observability.PlatformMetrics;
import com.projectanalytics.reporting.config.ReportingProperties;
import com.projectanalytics.reporting.persistence.ReportEntity;
import com.projectanalytics.reporting.persistence.ReportRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Purges report metadata and on-disk files older than configured retention (M10).
 * Reports remain immutable while retained; purge is an ops retention policy only.
 */
@Service
@ConditionalOnProperty(name = "projectanalytics.reporting.purge-enabled", havingValue = "true", matchIfMissing = true)
public class ReportRetentionService {

    private static final Logger log = LoggerFactory.getLogger(ReportRetentionService.class);

    private final ReportRepository reportRepository;
    private final ReportingProperties reportingProperties;
    private final PlatformMetrics platformMetrics;

    public ReportRetentionService(
            ReportRepository reportRepository,
            ReportingProperties reportingProperties,
            PlatformMetrics platformMetrics
    ) {
        this.reportRepository = reportRepository;
        this.reportingProperties = reportingProperties;
        this.platformMetrics = platformMetrics;
    }

    @Scheduled(cron = "${projectanalytics.reporting.purge-cron:0 30 2 * * *}")
    @Transactional
    public void purgeExpiredReports() {
        if (!reportingProperties.isPurgeEnabled()) {
            return;
        }
        int retentionDays = Math.max(1, reportingProperties.getRetentionDays());
        Instant cutoff = Instant.now().minus(retentionDays, ChronoUnit.DAYS);
        List<ReportEntity> expired = reportRepository.findByGeneratedAtBefore(cutoff);
        int deletedFiles = 0;
        int deletedRows = 0;
        for (ReportEntity report : expired) {
            if (deleteFileQuietly(report.getFilePath())) {
                deletedFiles++;
            }
            reportRepository.delete(report);
            deletedRows++;
        }
        if (deletedRows > 0) {
            platformMetrics.recordRetentionPurge("report", deletedRows);
            log.info(
                    "Report retention purge complete: retentionDays={} deletedRows={} deletedFiles={} cutoff={}",
                    retentionDays,
                    deletedRows,
                    deletedFiles,
                    cutoff
            );
        } else {
            log.debug("Report retention purge: nothing older than {} days (cutoff={})", retentionDays, cutoff);
        }
    }

    private boolean deleteFileQuietly(String filePath) {
        if (filePath == null || filePath.isBlank()) {
            return false;
        }
        try {
            Path path = Path.of(filePath);
            if (Files.isRegularFile(path)) {
                Files.delete(path);
                return true;
            }
        } catch (IOException exception) {
            log.warn("Failed to delete report file {}: {}", filePath, exception.getMessage());
        }
        return false;
    }
}
