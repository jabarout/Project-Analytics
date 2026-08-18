package com.projectanalytics.synchronization.application;

import com.projectanalytics.common.exception.BusinessException;
import com.projectanalytics.common.exception.ErrorCode;
import com.projectanalytics.infrastructure.openproject.OpenProjectAuthScheme;
import com.projectanalytics.infrastructure.openproject.OpenProjectConnectionProperties;
import com.projectanalytics.infrastructure.openproject.OpenProjectCredentials;
import com.projectanalytics.infrastructure.openproject.OpenProjectProperties;
import com.projectanalytics.infrastructure.security.SecretEncryptionService;
import com.projectanalytics.synchronization.api.dto.ConnectWorkspaceApiKeyRequest;
import com.projectanalytics.synchronization.persistence.WorkspaceCredentialEntity;
import com.projectanalytics.synchronization.persistence.WorkspaceCredentialRepository;
import com.projectanalytics.synchronization.persistence.WorkspaceEntity;
import com.projectanalytics.synchronization.persistence.WorkspaceRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Connects a PA user to an OpenProject instance with eligibility check (M14).
 * Stores credentials server-side; only Workspace Admins may rotate credentials on an existing connection.
 * OAuth and API-key paths share the same eligibility + membership rules.
 */
@Service
public class WorkspaceConnectionService {

    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceCredentialRepository credentialRepository;
    private final WorkspaceAccessService workspaceAccessService;
    private final OpenProjectEligibilityService eligibilityService;
    private final SecretEncryptionService secretEncryptionService;
    private final OpenProjectProperties openProjectProperties;
    private final boolean allowEnvApiKeyFallback;

    public WorkspaceConnectionService(
            WorkspaceRepository workspaceRepository,
            WorkspaceCredentialRepository credentialRepository,
            WorkspaceAccessService workspaceAccessService,
            OpenProjectEligibilityService eligibilityService,
            SecretEncryptionService secretEncryptionService,
            OpenProjectProperties openProjectProperties,
            @Value("${projectanalytics.security.openproject.allow-env-api-key-fallback:true}")
            boolean allowEnvApiKeyFallback
    ) {
        this.workspaceRepository = workspaceRepository;
        this.credentialRepository = credentialRepository;
        this.workspaceAccessService = workspaceAccessService;
        this.eligibilityService = eligibilityService;
        this.secretEncryptionService = secretEncryptionService;
        this.openProjectProperties = openProjectProperties;
        this.allowEnvApiKeyFallback = allowEnvApiKeyFallback;
    }

    /**
     * Connect using an API key (alternative to OAuth). Key is never returned to the client.
     */
    @Transactional
    public UUID connectWithApiKey(UUID userId, ConnectWorkspaceApiKeyRequest request) {
        String baseUrl = normalizeBaseUrl(request.baseUrl());
        final String apiKey = resolveApiKey(request.apiKey());

        OpenProjectConnectionProperties connection = new OpenProjectConnectionProperties(
                baseUrl,
                openProjectProperties.getTimeoutSeconds(),
                openProjectProperties.isVerifySsl(),
                OpenProjectCredentials.ofApiKey(apiKey)
        );

        OpenProjectEligibilityService.EligibilityResult eligibility = eligibilityService.evaluate(connection);
        if (!eligibility.eligible()) {
            throw new BusinessException(
                    ErrorCode.AUTH_006,
                    "Your OpenProject account does not have the required permissions to use Project Analytics. "
                            + eligibility.reason()
            );
        }

        StoredCredentialSecrets secrets = StoredCredentialSecrets.apiKey(apiKey);
        return finalizeEligibleConnect(userId, baseUrl, request.name(), eligibility, secrets);
    }

    /**
     * Completes connect after OAuth token exchange + the same eligibility check as API-key connect.
     * Persists per-workspace OAuth client id/secret (encrypted) with access/refresh tokens.
     */
    @Transactional
    public UUID connectWithOAuthTokens(
            UUID userId,
            String baseUrl,
            String requestedName,
            OpenProjectEligibilityService.EligibilityResult eligibility,
            String accessToken,
            String refreshToken,
            Instant tokenExpiresAt,
            String oauthClientId,
            String oauthClientSecret
    ) {
        if (!eligibility.eligible()) {
            throw new BusinessException(
                    ErrorCode.AUTH_006,
                    "Your OpenProject account does not have the required permissions to use Project Analytics. "
                            + eligibility.reason()
            );
        }
        if (accessToken == null || accessToken.isBlank()) {
            throw new BusinessException(ErrorCode.SYNC_005, "OpenProject access token is required.");
        }
        if (oauthClientId == null || oauthClientId.isBlank() || oauthClientSecret == null || oauthClientSecret.isBlank()) {
            throw new BusinessException(ErrorCode.SYNC_005, "OpenProject OAuth client id and secret are required.");
        }
        StoredCredentialSecrets secrets = StoredCredentialSecrets.oauth(
                accessToken,
                refreshToken,
                tokenExpiresAt,
                oauthClientId.trim(),
                oauthClientSecret
        );
        return finalizeEligibleConnect(userId, normalizeBaseUrl(baseUrl), requestedName, eligibility, secrets);
    }

    private UUID finalizeEligibleConnect(
            UUID userId,
            String baseUrl,
            String requestedName,
            OpenProjectEligibilityService.EligibilityResult eligibility,
            StoredCredentialSecrets secrets
    ) {
        Optional<WorkspaceEntity> existing = workspaceRepository.findByBaseUrlIgnoreCase(baseUrl);
        if (existing.isPresent()) {
            return connectExistingWorkspace(userId, existing.get(), eligibility, requestedName, secrets);
        }

        WorkspaceEntity workspace = workspaceRepository.save(
                new WorkspaceEntity(
                        requestedName == null || requestedName.isBlank()
                                ? defaultName(baseUrl)
                                : requestedName.trim(),
                        baseUrl
                )
        );
        saveCredential(workspace.getId(), userId, eligibility, secrets, true);
        workspaceAccessService.grantConnectorAdmin(workspace.getId(), userId);
        return workspace.getId();
    }

    private UUID connectExistingWorkspace(
            UUID userId,
            WorkspaceEntity workspace,
            OpenProjectEligibilityService.EligibilityResult eligibility,
            String requestedName,
            StoredCredentialSecrets secrets
    ) {
        boolean isAdmin = workspaceAccessService.isWorkspaceAdmin(workspace.getId(), userId);
        boolean hasAnalytics = workspaceAccessService.hasAnalyticsAccess(workspace.getId(), userId);
        boolean hasCredential = credentialRepository.existsByWorkspaceId(workspace.getId());

        if (isAdmin) {
            // Workspace Admin may rotate / set credentials after re-proving eligibility.
            saveCredential(workspace.getId(), userId, eligibility, secrets, !hasCredential);
            if (requestedName != null && !requestedName.isBlank()) {
                workspace.setName(requestedName.trim());
                workspaceRepository.save(workspace);
            }
            return workspace.getId();
        }

        if (hasAnalytics) {
            throw new BusinessException(
                    ErrorCode.AUTH_006,
                    "This OpenProject instance is already connected. Only a Workspace Admin can update credentials."
            );
        }

        throw new BusinessException(
                ErrorCode.AUTH_006,
                "This OpenProject instance is already connected to Project Analytics. "
                        + "Ask a Workspace Admin to grant you analytics access (M15)."
        );
    }

    private void saveCredential(
            UUID workspaceId,
            UUID userId,
            OpenProjectEligibilityService.EligibilityResult eligibility,
            StoredCredentialSecrets secrets,
            boolean createIfMissing
    ) {
        WorkspaceCredentialEntity credential = credentialRepository.findByWorkspaceId(workspaceId)
                .orElseGet(() -> {
                    if (!createIfMissing) {
                        throw new BusinessException(ErrorCode.SYNC_005, "Workspace credential is missing.");
                    }
                    return new WorkspaceCredentialEntity(
                            workspaceId,
                            secrets.authScheme().name(),
                            secretEncryptionService.encrypt(secrets.primarySecret()),
                            userId
                    );
                });
        credential.setAuthScheme(secrets.authScheme().name());
        credential.setSecretCiphertext(secretEncryptionService.encrypt(secrets.primarySecret()));
        if (secrets.refreshToken() != null && !secrets.refreshToken().isBlank()) {
            credential.setRefreshCiphertext(secretEncryptionService.encrypt(secrets.refreshToken()));
        } else {
            credential.setRefreshCiphertext(null);
        }
        if (secrets.authScheme() == OpenProjectAuthScheme.BEARER_TOKEN) {
            credential.setOauthClientId(secrets.oauthClientId());
            credential.setOauthClientSecretCiphertext(
                    secretEncryptionService.encrypt(secrets.oauthClientSecret())
            );
        }
        credential.setTokenExpiresAt(secrets.tokenExpiresAt());
        credential.setOpenProjectUserId(eligibility.openProjectUserId());
        credential.setOpenProjectLogin(eligibility.openProjectLogin());
        credential.setOpenProjectEmail(eligibility.openProjectEmail());
        credential.setOpenProjectAdmin(eligibility.openProjectAdmin());
        credential.setEligibilitySnapshot(trim(eligibility.reason(), 500));
        credentialRepository.save(credential);
    }

    /**
     * Persists refreshed OAuth tokens for an existing workspace credential (sync path).
     */
    @Transactional
    public void updateOAuthTokens(
            UUID workspaceId,
            String accessToken,
            String refreshToken,
            Instant tokenExpiresAt
    ) {
        WorkspaceCredentialEntity credential = credentialRepository.findByWorkspaceId(workspaceId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SYNC_005, "Workspace credential is missing."));
        credential.setAuthScheme(OpenProjectAuthScheme.BEARER_TOKEN.name());
        credential.setSecretCiphertext(secretEncryptionService.encrypt(accessToken));
        if (refreshToken != null && !refreshToken.isBlank()) {
            credential.setRefreshCiphertext(secretEncryptionService.encrypt(refreshToken));
        }
        credential.setTokenExpiresAt(tokenExpiresAt);
        credentialRepository.save(credential);
    }

    private String resolveApiKey(String requested) {
        String trimmed = requested == null ? "" : requested.trim();
        if (!trimmed.isBlank()) {
            return trimmed;
        }
        if (!allowEnvApiKeyFallback) {
            throw new BusinessException(
                    ErrorCode.SYNC_005,
                    "OpenProject API key is required. Env OPENPROJECT_API_KEY fallback is disabled in this environment."
            );
        }
        String envKey = openProjectProperties.getApiKey();
        if (envKey == null || envKey.isBlank()) {
            throw new BusinessException(
                    ErrorCode.SYNC_005,
                    "OpenProject API key is required (request body or OPENPROJECT_API_KEY for local fallback)."
            );
        }
        return envKey;
    }

    private static String defaultName(String baseUrl) {
        try {
            String host = java.net.URI.create(baseUrl).getHost();
            return host == null || host.isBlank() ? "OpenProject" : host;
        } catch (Exception exception) {
            return "OpenProject";
        }
    }

    static String normalizeBaseUrl(String baseUrl) {
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new BusinessException(ErrorCode.SYNC_005, "OpenProject base URL is required.");
        }
        String normalized = baseUrl.trim();
        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }

    private static String trim(String value, int max) {
        if (value == null) {
            return null;
        }
        return value.length() <= max ? value : value.substring(0, max);
    }

    private record StoredCredentialSecrets(
            OpenProjectAuthScheme authScheme,
            String primarySecret,
            String refreshToken,
            Instant tokenExpiresAt,
            String oauthClientId,
            String oauthClientSecret
    ) {
        static StoredCredentialSecrets apiKey(String apiKey) {
            return new StoredCredentialSecrets(OpenProjectAuthScheme.API_KEY, apiKey, null, null, null, null);
        }

        static StoredCredentialSecrets oauth(
                String accessToken,
                String refreshToken,
                Instant expiresAt,
                String oauthClientId,
                String oauthClientSecret
        ) {
            return new StoredCredentialSecrets(
                    OpenProjectAuthScheme.BEARER_TOKEN,
                    accessToken,
                    refreshToken,
                    expiresAt,
                    oauthClientId,
                    oauthClientSecret
            );
        }
    }
}
