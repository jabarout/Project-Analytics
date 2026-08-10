package com.projectanalytics.analytics.configuration;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(AnalyticsScoringProperties.class)
public class AnalyticsConfiguration {
}
