package com.projectanalytics.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * JPA and transaction configuration for PostgreSQL persistence.
 */
@Configuration
@EnableJpaRepositories(basePackages = "com.projectanalytics")
@EnableTransactionManagement
public class JpaConfiguration {
}
