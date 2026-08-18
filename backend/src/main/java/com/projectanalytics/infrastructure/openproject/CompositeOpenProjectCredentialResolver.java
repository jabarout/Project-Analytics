package com.projectanalytics.infrastructure.openproject;

import com.projectanalytics.common.exception.BusinessException;
import com.projectanalytics.common.exception.ErrorCode;
import com.projectanalytics.infrastructure.security.SecretEncryptionService;
import com.projectanalytics.synchronization.application.WorkspaceConnectionService;
import com.projectanalytics.synchronization.persistence.WorkspaceCredentialEntity;
import com.projectanalytics.synchronization.persistence.WorkspaceCredentialRepository;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

/**
 * Prefers per-workspace encrypted credentials (M14).
 * Refreshes OAuth access tokens when near expiry when a refresh token is present.
 * Env API-key fallback is local/dev/test only (Phase 2).
 */
@Component
@Primary
public class CompositeOpenProjectCredentialResolver implements OpenProjectCredentialResolver {

    private static final long REFRESH_SKEW_SECONDS = 120;

    private final WorkspaceCredentialRepository credentialRepository;
    private final SecretEncryptionService secretEncryptionService;
    private final EnvironmentApiKeyOpenProjectCredentialResolver environmentFallback;
    private final OpenProjectProperties openProjectProperties;
    private final OpenProjectOAuthProperties oauthProperties;
    private final OpenProjectOAuthClient oauthClient;
    private final ObjectProvider<WorkspaceConnectionService> workspaceConnectionService;
    private final boolean allowEnvApiKeyFallback;

    public CompositeOpenProjectCredentialResolver(
            WorkspaceCredentialRepository credentialRepository,
            SecretEncryptionService secretEncryptionService,
            EnvironmentApiKeyOpenProjectCredentialResolver environmentFallback,
            OpenProjectProperties openProjectProperties,
            OpenProjectOAuthProperties oauthProperties,
            OpenProjectOAuthClient oauthClient,
            ObjectProvider<WorkspaceConnectionService> workspaceConnectionService,
            @Value("${projectanalytics.security.openproject.allow-env-api-key-fallback:true}")
            boolean allowEnvApiKeyFallback
    ) {
        this.credentialRepository = credentialRepository;
        this.secretEncryptionService = secretEncryptionService;
        this.environmentFallback = environmentFallback;
        this.openProjectProperties = openProjectProperties;
        this.oauthProperties = oauthProperties;
        this.oauthClient = oauthClient;
        this.workspaceConnectionService = workspaceConnectionService;
        this.allowEnvApiKeyFallback = allowEnvApiKeyFallback;
    }

    @Override
    public OpenProjectConnectionProperties resolve(UUID workspaceId, String workspaceBaseUrl) {
        return credentialRepository.findByWorkspaceId(workspaceId)
                .map(credential -> fromStored(workspaceId, credential, workspaceBaseUrl))
                .orElseGet(() -> resolveFallback(workspaceId, workspaceBaseUrl));
    }

    private OpenProjectConnectionProperties resolveFallback(UUID workspaceId, String workspaceBaseUrl) {
        if (!allowEnvApiKeyFallback) {
            throw new BusinessException(
                    ErrorCode.SYNC_005,
                    "No stored OpenProject credential for this workspace. "
                            + "Connect via API key or OAuth (env OPENPROJECT_API_KEY fallback is disabled)."
            );
        }
        return environmentFallback.resolve(workspaceId, workspaceBaseUrl);
    }

    private OpenProjectConnectionProperties fromStored(
            UUID workspaceId,
            WorkspaceCredentialEntity credential,
            String workspaceBaseUrl
    ) {
        String baseUrl = workspaceBaseUrl == null || workspaceBaseUrl.isBlank()
                ? null
                : normalize(workspaceBaseUrl);
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new BusinessException(ErrorCode.SYNC_005, "OpenProject base URL is required.");
        }

        if (OpenProjectAuthScheme.BEARER_TOKEN.name().equalsIgnoreCase(credential.getAuthScheme())) {
            maybeRefreshOAuth(workspaceId, credential, baseUrl);
            // Re-read after possible refresh
            credential = credentialRepository.findByWorkspaceId(workspaceId).orElse(credential);
        }

        String secret = secretEncryptionService.decrypt(credential.getSecretCiphertext());
        if (secret == null || secret.isBlank()) {
            throw new BusinessException(ErrorCode.SYNC_005, "Stored OpenProject credential is empty.");
        }
        OpenProjectCredentials credentials;
        if (OpenProjectAuthScheme.BEARER_TOKEN.name().equalsIgnoreCase(credential.getAuthScheme())) {
            credentials = OpenProjectCredentials.ofBearerToken(secret);
        } else {
            credentials = OpenProjectCredentials.ofApiKey(secret);
        }
        return new OpenProjectConnectionProperties(
                baseUrl,
                openProjectProperties.getTimeoutSeconds(),
                openProjectProperties.isVerifySsl(),
                credentials
        );
    }

    private void maybeRefreshOAuth(UUID workspaceId, WorkspaceCredentialEntity credential, String baseUrl) {
        Instant expiresAt = credential.getTokenExpiresAt();
        if (expiresAt == null || expiresAt.isAfter(Instant.now().plusSeconds(REFRESH_SKEW_SECONDS))) {
            return;
        }
        String refreshCipher = credential.getRefreshCiphertext();
        if (refreshCipher == null || refreshCipher.isBlank()) {
            throw new BusinessException(
                    ErrorCode.SYNC_005,
                    "OpenProject OAuth access token expired and no refresh token is stored. Reconnect via OAuth."
            );
        }
        OpenProjectOAuthClientCredentials client = resolveOAuthClient(credential);
        String refreshToken = secretEncryptionService.decrypt(refreshCipher);
        OpenProjectOAuthTokenResponse refreshed = oauthClient.refreshAccessToken(baseUrl, refreshToken, client);
        WorkspaceConnectionService connectionService = workspaceConnectionService.getObject();
        connectionService.updateOAuthTokens(
                workspaceId,
                refreshed.accessToken(),
                refreshed.refreshToken() != null ? refreshed.refreshToken() : refreshToken,
                refreshed.expiresAt()
        );
    }

    /**
     * Prefer per-workspace OAuth client credentials; optional global env only if workspace has none.
     */
    private OpenProjectOAuthClientCredentials resolveOAuthClient(WorkspaceCredentialEntity credential) {
        String clientId = credential.getOauthClientId();
        String secretCipher = credential.getOauthClientSecretCiphertext();
        if (clientId != null && !clientId.isBlank() && secretCipher != null && !secretCipher.isBlank()) {
            return new OpenProjectOAuthClientCredentials(
                    clientId.trim(),
                    secretEncryptionService.decrypt(secretCipher)
            );
        }
        if (oauthProperties.hasGlobalClientDefaults()) {
            return new OpenProjectOAuthClientCredentials(
                    oauthProperties.getClientId().trim(),
                    oauthProperties.getClientSecret().trim()
            );
        }
        throw new BusinessException(
                ErrorCode.SYNC_005,
                "OpenProject OAuth access token expired and no OAuth client credentials are stored for this workspace. Reconnect via OAuth."
        );
    }

    private static String normalize(String baseUrl) {
        String normalized = baseUrl.trim();
        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }
}
