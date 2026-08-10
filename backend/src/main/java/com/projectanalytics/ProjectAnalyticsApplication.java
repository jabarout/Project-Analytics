package com.projectanalytics;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the Project Analytics backend.
 *
 * <p>Business logic modules are organized by feature under {@code com.projectanalytics}.
 * OpenProject remains the operational source of truth; this application provides analytics
 * and decision intelligence only.
 */
@SpringBootApplication
public class ProjectAnalyticsApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProjectAnalyticsApplication.class, args);
    }
}
