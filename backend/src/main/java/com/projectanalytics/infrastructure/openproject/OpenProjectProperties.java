package com.projectanalytics.infrastructure.openproject;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Externalized OpenProject connection settings (Configuration documentation).
 * API keys must never be committed; provide via environment variables.
 */
@ConfigurationProperties(prefix = "projectanalytics.openproject")
public class OpenProjectProperties {

    /**
     * Optional default base URL used when creating workspaces without an explicit URL.
     */
    private String url = "";

    /**
     * API key for Basic authentication (username {@code apikey}).
     */
    private String apiKey = "";

    /**
     * HTTP timeout in seconds.
     */
    private int timeoutSeconds = 30;

    /**
     * Whether TLS certificates are verified.
     */
    private boolean verifySsl = true;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public int getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public void setTimeoutSeconds(int timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }

    public boolean isVerifySsl() {
        return verifySsl;
    }

    public void setVerifySsl(boolean verifySsl) {
        this.verifySsl = verifySsl;
    }
}
