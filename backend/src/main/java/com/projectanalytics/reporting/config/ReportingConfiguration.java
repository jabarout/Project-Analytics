package com.projectanalytics.reporting.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(ReportingProperties.class)
public class ReportingConfiguration {
}
