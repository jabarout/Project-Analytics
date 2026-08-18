package com.projectanalytics.infrastructure.openproject;

import java.time.Instant;

/**
 * Tokens returned by OpenProject {@code POST /oauth/token}.
 */
public record OpenProjectOAuthTokenResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        String scope,
        Instant expiresAt
) {
}
