package com.projectanalytics.observability;

import com.projectanalytics.synchronization.domain.SynchronizationStatus;
import com.projectanalytics.synchronization.persistence.SynchronizationHistoryRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Custom business metrics under the frozen {@code pa_*} namespace.
 * Low-cardinality labels only (status, type, format) — never project/user ids.
 */
@Component
public class PlatformMetrics {

    private final MeterRegistry registry;
    private final SynchronizationHistoryRepository historyRepository;
    private final AtomicReference<Instant> lastSyncSuccessAt = new AtomicReference<>();

    public PlatformMetrics(MeterRegistry registry, SynchronizationHistoryRepository historyRepository) {
        this.registry = registry;
        this.historyRepository = historyRepository;
        registry.gauge(
                ObservabilityConfiguration.METRIC_PREFIX + "sync_last_success_age_seconds",
                this,
                PlatformMetrics::lastSuccessAgeSeconds
        );
    }

    public void recordSyncSuccess(String syncType, long durationMs, int projects, int workPackages) {
        lastSyncSuccessAt.set(Instant.now());
        Counter.builder(ObservabilityConfiguration.METRIC_PREFIX + "sync_runs_total")
                .tag("status", "success")
                .tag("type", safe(syncType))
                .register(registry)
                .increment();
        Timer.builder(ObservabilityConfiguration.METRIC_PREFIX + "sync_duration_seconds")
                .tag("status", "success")
                .tag("type", safe(syncType))
                .register(registry)
                .record(durationMs, TimeUnit.MILLISECONDS);
        registry.counter(ObservabilityConfiguration.METRIC_PREFIX + "sync_projects_total", "type", safe(syncType))
                .increment(projects);
        registry.counter(ObservabilityConfiguration.METRIC_PREFIX + "sync_work_packages_total", "type", safe(syncType))
                .increment(workPackages);
    }

    public void recordSyncFailure(String syncType, long durationMs) {
        Counter.builder(ObservabilityConfiguration.METRIC_PREFIX + "sync_runs_total")
                .tag("status", "failure")
                .tag("type", safe(syncType))
                .register(registry)
                .increment();
        Timer.builder(ObservabilityConfiguration.METRIC_PREFIX + "sync_duration_seconds")
                .tag("status", "failure")
                .tag("type", safe(syncType))
                .register(registry)
                .record(durationMs, TimeUnit.MILLISECONDS);
    }

    public void recordAnalyticsRecalculation(long durationMs, int projectsScored) {
        Timer.builder(ObservabilityConfiguration.METRIC_PREFIX + "analytics_recalculate_duration_seconds")
                .register(registry)
                .record(durationMs, TimeUnit.MILLISECONDS);
        registry.counter(ObservabilityConfiguration.METRIC_PREFIX + "analytics_projects_scored_total")
                .increment(projectsScored);
    }

    public void recordReportGenerated(String reportType, String format, String status, long durationMs) {
        Counter.builder(ObservabilityConfiguration.METRIC_PREFIX + "report_generated_total")
                .tag("type", safe(reportType))
                .tag("format", safe(format))
                .tag("status", safe(status))
                .register(registry)
                .increment();
        Timer.builder(ObservabilityConfiguration.METRIC_PREFIX + "report_generation_duration_seconds")
                .tag("type", safe(reportType))
                .tag("format", safe(format))
                .tag("status", safe(status))
                .register(registry)
                .record(durationMs, TimeUnit.MILLISECONDS);
    }

    public void recordRecommendationEvaluation(String scopeType, int recommendationCount) {
        Counter.builder(ObservabilityConfiguration.METRIC_PREFIX + "recommendation_evaluations_total")
                .tag("scope", safe(scopeType))
                .register(registry)
                .increment();
        registry.counter(ObservabilityConfiguration.METRIC_PREFIX + "recommendation_items_total", "scope", safe(scopeType))
                .increment(recommendationCount);
    }

    public void recordRetentionPurge(String target, int deletedRows) {
        Counter.builder(ObservabilityConfiguration.METRIC_PREFIX + "retention_purge_total")
                .tag("target", safe(target))
                .register(registry)
                .increment(deletedRows);
    }

    private double lastSuccessAgeSeconds() {
        Instant cached = lastSyncSuccessAt.get();
        if (cached != null) {
            return Math.max(0, Duration.between(cached, Instant.now()).getSeconds());
        }
        return historyRepository.findFirstByStatusOrderByFinishedAtDesc(SynchronizationStatus.SUCCESS)
                .map(history -> {
                    Instant finished = history.getFinishedAt();
                    if (finished == null) {
                        return Double.NaN;
                    }
                    lastSyncSuccessAt.compareAndSet(null, finished);
                    return (double) Math.max(0, Duration.between(finished, Instant.now()).getSeconds());
                })
                .orElse(Double.POSITIVE_INFINITY);
    }

    private static String safe(String value) {
        if (value == null || value.isBlank()) {
            return "unknown";
        }
        return value.toLowerCase();
    }
}
