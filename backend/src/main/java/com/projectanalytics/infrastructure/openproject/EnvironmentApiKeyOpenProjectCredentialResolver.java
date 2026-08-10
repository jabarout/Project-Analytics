package com.projectanalytics.infrastructure.openproject;

import com.projectanalytics.common.exception.BusinessException;
import com.projectanalytics.common.exception.ErrorCode;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Default credential resolver: platform-level OpenProject API key from configuration.
 *
 * <p>No per-user or OAuth behaviour. Behaviour matches Milestone 3.
 */
@Component
public class EnvironmentApiKeyOpenProjectCredentialResolver implements OpenProjectCredentialResolver {

    private final OpenProjectProperties openProjectProperties;

    public EnvironmentApiKeyOpenProjectCredentialResolver(OpenProjectProperties openProjectProperties) {
        this.openProjectProperties = openProjectProperties;
    }

    @Override
    public OpenProjectConnectionProperties resolve(UUID workspaceId, String workspaceBaseUrl) {
        String apiKey = openProjectProperties.getApiKey();
        if (apiKey == null || apiKey.isBlank()) {
            throw new BusinessException(
                    ErrorCode.SYNC_005,
                    "OpenProject API key is not configured in the running backend process. "
                            + "Set OPENPROJECT_API_KEY in .env (or the process environment), then restart the backend "
                            + "with ./scripts/run-backend.sh so the key is loaded. Editing .env alone does not update "
                            + "an already-running JVM. The Connections URL is the OpenProject base URL; the API key "
                            + "always comes from the environment (not the UI)."
            );
        }
        String baseUrl = normalizeBaseUrl(workspaceBaseUrl);
        return new OpenProjectConnectionProperties(
                baseUrl,
                openProjectProperties.getTimeoutSeconds(),
                openProjectProperties.isVerifySsl(),
                OpenProjectCredentials.ofApiKey(apiKey)
        );
    }

    private static String normalizeBaseUrl(String baseUrl) {
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new BusinessException(ErrorCode.SYNC_005, "OpenProject base URL is required.");
        }
        String normalized = baseUrl.trim();
        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }
}
