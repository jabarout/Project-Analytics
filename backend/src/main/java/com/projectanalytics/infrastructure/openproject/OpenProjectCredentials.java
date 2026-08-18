package com.projectanalytics.infrastructure.openproject;

/**
 * Credentials used for a single OpenProject HTTP session.
 *
 * <p>Only one of {@code apiKey} or {@code accessToken} is populated depending on
 * {@link #scheme()}. Secrets must never be logged.
 */
public record OpenProjectCredentials(
        OpenProjectAuthScheme scheme,
        String apiKey,
        String accessToken
) {

    public static OpenProjectCredentials ofApiKey(String apiKey) {
        return new OpenProjectCredentials(OpenProjectAuthScheme.API_KEY, apiKey, null);
    }

    /**
     * Factory for OAuth 2.0 access tokens (M14 Phase 7).
     */
    public static OpenProjectCredentials ofBearerToken(String accessToken) {
        return new OpenProjectCredentials(OpenProjectAuthScheme.BEARER_TOKEN, null, accessToken);
    }
}
