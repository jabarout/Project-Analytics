package com.projectanalytics.synchronization.application;

import com.projectanalytics.common.exception.BusinessException;
import com.projectanalytics.common.exception.ErrorCode;
import com.projectanalytics.infrastructure.openproject.OpenProjectConnectionProperties;
import com.projectanalytics.infrastructure.openproject.OpenProjectCredentials;
import com.projectanalytics.infrastructure.openproject.OpenProjectOAuthClient;
import com.projectanalytics.infrastructure.openproject.OpenProjectOAuthClientCredentials;
import com.projectanalytics.infrastructure.openproject.OpenProjectOAuthProperties;
import com.projectanalytics.infrastructure.openproject.OpenProjectOAuthTokenResponse;
import com.projectanalytics.infrastructure.openproject.OpenProjectProperties;
import com.projectanalytics.infrastructure.security.SecretEncryptionService;
import com.projectanalytics.synchronization.api.dto.OAuthConnectStatusResponse;
import com.projectanalytics.synchronization.api.dto.StartOAuthConnectRequest;
import com.projectanalytics.synchronization.api.dto.StartOAuthConnectResponse;
import com.projectanalytics.synchronization.persistence.OAuthConnectPendingEntity;
import com.projectanalytics.synchronization.persistence.OAuthConnectPendingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

/**
 * OpenProject OAuth 2.0 authorization-code + PKCE connect (multi-OP).
 * Per-connect client id/secret are retained in pending state (secret encrypted),
 * then stored on the workspace. Global env client defaults are optional fallback only.
 */
@Service
public class WorkspaceOAuthConnectService {

    private static final Logger log = LoggerFactory.getLogger(WorkspaceOAuthConnectService.class);
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final OpenProjectOAuthProperties oauthProperties;
    private final OpenProjectProperties openProjectProperties;
    private final OAuthConnectPendingRepository pendingRepository;
    private final OpenProjectOAuthClient oauthClient;
    private final OpenProjectEligibilityService eligibilityService;
    private final WorkspaceConnectionService workspaceConnectionService;
    private final SecretEncryptionService secretEncryptionService;
    private final TransactionTemplate transactionTemplate;

    public WorkspaceOAuthConnectService(
            OpenProjectOAuthProperties oauthProperties,
            OpenProjectProperties openProjectProperties,
            OAuthConnectPendingRepository pendingRepository,
            OpenProjectOAuthClient oauthClient,
            OpenProjectEligibilityService eligibilityService,
            WorkspaceConnectionService workspaceConnectionService,
            SecretEncryptionService secretEncryptionService,
            PlatformTransactionManager transactionManager
    ) {
        this.oauthProperties = oauthProperties;
        this.openProjectProperties = openProjectProperties;
        this.pendingRepository = pendingRepository;
        this.oauthClient = oauthClient;
        this.eligibilityService = eligibilityService;
        this.workspaceConnectionService = workspaceConnectionService;
        this.secretEncryptionService = secretEncryptionService;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    @Transactional(readOnly = true)
    public OAuthConnectStatusResponse status() {
        return new OAuthConnectStatusResponse(
                oauthProperties.isEnabled(),
                blankToNull(oauthProperties.getRedirectUri()),
                oauthProperties.hasGlobalClientDefaults()
        );
    }

    @Transactional
    public StartOAuthConnectResponse start(UUID userId, StartOAuthConnectRequest request) {
        requireEnabled();
        String baseUrl = WorkspaceConnectionService.normalizeBaseUrl(request.baseUrl());
        OpenProjectOAuthClientCredentials client = resolveClientCredentials(
                request.clientId(),
                request.clientSecret()
        );
        pendingRepository.deleteExpired(Instant.now().getEpochSecond());

        String state = randomUrlSafe(32);
        String codeVerifier = randomUrlSafe(64);
        Instant expiresAt = Instant.now().plusSeconds(Math.max(60, oauthProperties.getStateTtlSeconds()));

        pendingRepository.saveAndFlush(new OAuthConnectPendingEntity(
                state,
                codeVerifier,
                userId,
                baseUrl,
                blankToNull(request.name() == null ? null : request.name().trim()),
                client.clientId(),
                secretEncryptionService.encrypt(client.clientSecret()),
                expiresAt
        ));

        String authorizationUrl = UriComponentsBuilder
                .fromUriString(baseUrl + "/oauth/authorize")
                .queryParam("response_type", "code")
                .queryParam("client_id", client.clientId())
                .queryParam("redirect_uri", oauthProperties.getRedirectUri())
                .queryParam("scope", oauthProperties.getScopes())
                .queryParam("state", state)
                .queryParam("code_challenge", pkceChallengeS256(codeVerifier))
                .queryParam("code_challenge_method", "S256")
                .queryParam("prompt", "consent")
                .encode()
                .build()
                .toUriString();

        return new StartOAuthConnectResponse(authorizationUrl, state, true);
    }

    /**
     * Completes the OAuth redirect. Not {@code @Transactional} so AUTH_006 denials become
     * oauth=error redirects instead of SYSTEM_001.
     */
    public String completeCallback(String code, String state, String oauthError, String oauthErrorDescription) {
        if (oauthError != null && !oauthError.isBlank()) {
            String detail = oauthErrorDescription == null || oauthErrorDescription.isBlank()
                    ? oauthError
                    : oauthError + ": " + oauthErrorDescription;
            return errorRedirect(detail);
        }
        if (code == null || code.isBlank() || state == null || state.isBlank()) {
            return errorRedirect("Missing authorization code or state.");
        }
        if (!oauthProperties.isEnabled()) {
            return errorRedirect("OpenProject OAuth is not configured (redirect URI missing).");
        }

        OAuthConnectPendingEntity pending = consumePendingState(state);
        if (pending == null) {
            return errorRedirect("OAuth state is invalid or already used.");
        }
        if (pending.isExpired(Instant.now())) {
            return errorRedirect("OAuth state expired. Start connect again.");
        }

        try {
            String clientSecret = secretEncryptionService.decrypt(pending.getOauthClientSecretCiphertext());
            OpenProjectOAuthClientCredentials client = new OpenProjectOAuthClientCredentials(
                    pending.getOauthClientId(),
                    clientSecret
            );

            OpenProjectOAuthTokenResponse tokens = oauthClient.exchangeAuthorizationCode(
                    pending.getBaseUrl(),
                    code,
                    pending.getCodeVerifier(),
                    client
            );

            OpenProjectConnectionProperties connection = new OpenProjectConnectionProperties(
                    pending.getBaseUrl(),
                    openProjectProperties.getTimeoutSeconds(),
                    openProjectProperties.isVerifySsl(),
                    OpenProjectCredentials.ofBearerToken(tokens.accessToken())
            );
            OpenProjectEligibilityService.EligibilityResult eligibility = eligibilityService.evaluate(connection);
            if (!eligibility.eligible()) {
                return errorRedirect(
                        "OpenProject account is not eligible to connect. " + eligibility.reason()
                );
            }

            workspaceConnectionService.connectWithOAuthTokens(
                    pending.getUserId(),
                    pending.getBaseUrl(),
                    pending.getWorkspaceName(),
                    eligibility,
                    tokens.accessToken(),
                    tokens.refreshToken(),
                    tokens.expiresAt(),
                    client.clientId(),
                    client.clientSecret()
            );
            return oauthProperties.getFrontendSuccessUrl();
        } catch (BusinessException exception) {
            return errorRedirect(exception.getMessage());
        } catch (Exception exception) {
            log.warn("OAuth connect failed unexpectedly: {}", exception.toString());
            return errorRedirect("OAuth connect failed.");
        }
    }

    private OAuthConnectPendingEntity consumePendingState(String state) {
        return transactionTemplate.execute(status -> {
            pendingRepository.deleteExpired(Instant.now().getEpochSecond());
            OAuthConnectPendingEntity pending = pendingRepository.findByStateToken(state).orElse(null);
            if (pending == null) {
                return null;
            }
            pendingRepository.delete(pending);
            pendingRepository.flush();
            return pending;
        });
    }

    private OpenProjectOAuthClientCredentials resolveClientCredentials(String requestedId, String requestedSecret) {
        String id = requestedId == null ? "" : requestedId.trim();
        String secret = requestedSecret == null ? "" : requestedSecret.trim();
        if (!id.isBlank() && !secret.isBlank()) {
            return new OpenProjectOAuthClientCredentials(id, secret);
        }
        if (oauthProperties.hasGlobalClientDefaults()) {
            // Optional local/single-tenant fallback — never used when per-connect values are provided.
            return new OpenProjectOAuthClientCredentials(
                    oauthProperties.getClientId().trim(),
                    oauthProperties.getClientSecret().trim()
            );
        }
        throw new BusinessException(
                ErrorCode.SYNC_005,
                "OpenProject OAuth client id and secret are required. Create an OAuth application in "
                        + "OpenProject (Administration → Authentication → OAuth applications) using redirect URI "
                        + oauthProperties.getRedirectUri()
                        + ", then paste the client id and secret here."
        );
    }

    private void requireEnabled() {
        if (!oauthProperties.isEnabled()) {
            throw new BusinessException(
                    ErrorCode.SYNC_005,
                    "OpenProject OAuth is not configured. Set OPENPROJECT_OAUTH_REDIRECT_URI."
            );
        }
    }

    private String errorRedirect(String message) {
        String base = oauthProperties.getFrontendErrorUrl();
        String safe = message == null ? "OAuth connect failed." : message;
        if (safe.length() > 300) {
            safe = safe.substring(0, 300);
        }
        String encoded = URLEncoder.encode(safe, StandardCharsets.UTF_8);
        if (base.contains("?")) {
            return base + "&message=" + encoded;
        }
        return base + "?message=" + encoded;
    }

    private static String pkceChallengeS256(String verifier) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(verifier.getBytes(StandardCharsets.US_ASCII));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
        } catch (Exception exception) {
            throw new IllegalStateException("SHA-256 not available", exception);
        }
    }

    private static String randomUrlSafe(int bytes) {
        byte[] buffer = new byte[bytes];
        SECURE_RANDOM.nextBytes(buffer);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(buffer);
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
