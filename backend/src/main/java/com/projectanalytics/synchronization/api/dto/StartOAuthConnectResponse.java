package com.projectanalytics.synchronization.api.dto;

/**
 * Browser should navigate to {@code authorizationUrl} to complete OpenProject consent.
 */
public record StartOAuthConnectResponse(
        String authorizationUrl,
        String state,
        boolean oauthConfigured
) {
}
