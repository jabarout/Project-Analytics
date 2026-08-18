package com.projectanalytics.synchronization.api.dto;

/**
 * Whether OAuth connect is available and setup hints for the Connections UI.
 * Never includes client secrets.
 */
public record OAuthConnectStatusResponse(
        /** True when PA redirect URI is configured (multi-OP OAuth UI may be shown). */
        boolean enabled,
        /** Redirect URI to register in each OpenProject OAuth application. */
        String redirectUri,
        /**
         * True when global OPENPROJECT_OAUTH_CLIENT_ID/SECRET are set as optional defaults.
         * Per-workspace credentials always win when provided.
         */
        boolean globalClientDefaultsAvailable
) {
}
