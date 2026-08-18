package com.projectanalytics.synchronization.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Connect OpenProject with an API key (M14 alternative to OAuth).
 * apiKey may be omitted in local/dev to use OPENPROJECT_API_KEY fallback.
 */
public record ConnectWorkspaceApiKeyRequest(
        @NotBlank @Size(max = 500) String baseUrl,
        @Size(max = 200) String name,
        @Size(max = 500) String apiKey
) {
}
