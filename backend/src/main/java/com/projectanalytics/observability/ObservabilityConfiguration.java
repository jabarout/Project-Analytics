package com.projectanalytics.observability;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.config.MeterFilter;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * M9 observability wiring. Additive only — no product domain changes.
 *
 * <p>Metrics use the frozen {@code pa_*} namespace for custom business meters.
 * Micrometer is the bridge for a future OpenTelemetry exporter (tracing deferred).
 */
@Configuration
public class ObservabilityConfiguration {

    public static final String METRIC_PREFIX = "pa_";

    @Bean
    public MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
        return registry -> registry.config()
                .commonTags("application", "project-analytics-backend")
                // Deny high-cardinality tags if accidentally introduced on pa_* meters
                .meterFilter(MeterFilter.deny(id -> {
                    String name = id.getName();
                    if (!name.startsWith(METRIC_PREFIX)) {
                        return false;
                    }
                    return id.getTags().stream().anyMatch(tag ->
                            "projectId".equals(tag.getKey())
                                    || "projectName".equals(tag.getKey())
                                    || "userId".equals(tag.getKey())
                    );
                }));
    }
}
