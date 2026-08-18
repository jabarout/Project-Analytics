package com.projectanalytics.infrastructure.openproject;

/**
 * OAuth application credentials for a specific OpenProject instance (not user tokens).
 */
public record OpenProjectOAuthClientCredentials(String clientId, String clientSecret) {

    public OpenProjectOAuthClientCredentials {
        if (clientId == null || clientId.isBlank()) {
            throw new IllegalArgumentException("OAuth client id is required");
        }
        if (clientSecret == null || clientSecret.isBlank()) {
            throw new IllegalArgumentException("OAuth client secret is required");
        }
    }
}
