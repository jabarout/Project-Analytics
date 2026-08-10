package com.projectanalytics.analytics.application;

import com.projectanalytics.analytics.configuration.AnalyticsScoringProperties;
import com.projectanalytics.analytics.persistence.AnalyticsSnapshotRepository;
import com.projectanalytics.observability.PlatformMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Purges historical analytics snapshots older than configured retention (M10).
 * Does not touch the latest {@code analytics} row per project.
 */
@Service
@ConditionalOnProperty(
        name = "projectanalytics.analytics.snapshot-purge-enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class AnalyticsSnapshotRetentionService {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsSnapshotRetentionService.class);

    private final AnalyticsSnapshotRepository snapshotRepository;
    private final AnalyticsScoringProperties analyticsProperties;
    private final PlatformMetrics platformMetrics;

    public AnalyticsSnapshotRetentionService(
            AnalyticsSnapshotRepository snapshotRepository,
            AnalyticsScoringProperties analyticsProperties,
            PlatformMetrics platformMetrics
    ) {
        this.snapshotRepository = snapshotRepository;
        this.analyticsProperties = analyticsProperties;
        this.platformMetrics = platformMetrics;
    }

    @Scheduled(cron = "${projectanalytics.analytics.snapshot-purge-cron:0 0 3 * * *}")
    @Transactional
    public void purgeExpiredSnapshots() {
        if (!analyticsProperties.isSnapshotPurgeEnabled()) {
            return;
        }
        int retentionDays = Math.max(1, analyticsProperties.getSnapshotRetentionDays());
        Instant cutoff = Instant.now().minus(retentionDays, ChronoUnit.DAYS);
        int deleted = snapshotRepository.deleteByCalculatedAtBefore(cutoff);
        if (deleted > 0) {
            platformMetrics.recordRetentionPurge("analytics_snapshot", deleted);
            log.info(
                    "Analytics snapshot retention purge complete: retentionDays={} deletedRows={} cutoff={}",
                    retentionDays,
                    deleted,
                    cutoff
            );
        } else {
            log.debug(
                    "Analytics snapshot retention purge: nothing older than {} days (cutoff={})",
                    retentionDays,
                    cutoff
            );
        }
    }
}
