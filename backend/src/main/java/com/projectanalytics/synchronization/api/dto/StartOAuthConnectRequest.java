package com.projectanalytics.synchronization.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Starts OpenProject OAuth authorization-code + PKCE connect for a PA user.
 *
 * <p>{@code clientId}/{@code clientSecret} identify the OAuth application registered
 * on that OpenProject instance. When omitted, optional global env defaults may be used
 * (local/single-tenant only).
 */
public record StartOAuthConnectRequest(
        @NotBlank @Size(max = 500) String baseUrl,
        @Size(max = 200) String name,
        @Size(max = 200) String clientId,
        @Size(max = 500) String clientSecret
) {
}
