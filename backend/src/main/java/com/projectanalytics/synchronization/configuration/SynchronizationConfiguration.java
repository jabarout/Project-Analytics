package com.projectanalytics.synchronization.configuration;

import com.projectanalytics.synchronization.application.SynchronizationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@EnableConfigurationProperties(SynchronizationProperties.class)
public class SynchronizationConfiguration {
}
