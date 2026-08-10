package com.projectanalytics.infrastructure.openproject;

/**
 * Connection parameters for a single OpenProject workspace request.
 *
 * <p>Credentials are resolved by {@link OpenProjectCredentialResolver} so the synchronization
 * engine never hard-codes how OpenProject is authenticated.
 */
public record OpenProjectConnectionProperties(
        String baseUrl,
        int timeoutSeconds,
        boolean verifySsl,
        OpenProjectCredentials credentials
) {
}
