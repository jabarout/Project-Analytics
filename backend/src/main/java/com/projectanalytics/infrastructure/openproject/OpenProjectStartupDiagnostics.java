package com.projectanalytics.infrastructure.openproject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Logs whether OpenProject credentials are present at startup (never logs the key value).
 * Helps catch the common failure mode: .env edited but backend not restarted with env loaded.
 */
@Component
@Order(50)
public class OpenProjectStartupDiagnostics implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(OpenProjectStartupDiagnostics.class);

    private final OpenProjectProperties openProjectProperties;

    public OpenProjectStartupDiagnostics(OpenProjectProperties openProjectProperties) {
        this.openProjectProperties = openProjectProperties;
    }

    @Override
    public void run(ApplicationArguments args) {
        String apiKey = openProjectProperties.getApiKey();
        String url = openProjectProperties.getUrl();
        boolean keyConfigured = apiKey != null && !apiKey.isBlank();
        boolean urlConfigured = url != null && !url.isBlank();

        if (keyConfigured) {
            log.info(
                    "OpenProject credentials: API key configured (length={}), default URL={}",
                    apiKey.length(),
                    urlConfigured ? url : "(unset — use workspace base URL from Connections)"
            );
        } else {
            log.warn(
                    "OpenProject API key is NOT configured in this process. Synchronization will fail with "
                            + "'OpenProject API key is not configured'. Set OPENPROJECT_API_KEY and restart via "
                            + "./scripts/run-backend.sh (Spring does not auto-reload .env)."
            );
        }
    }
}
