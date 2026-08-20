package com.projectanalytics.infrastructure.openproject;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * OpenProject OAuth 2.0 settings (authorization code + PKCE).
 *
 * <p>Multi-OP: client id/secret are normally supplied per connect and stored per workspace.
 * Global client-id/secret remain optional local/single-tenant defaults and never override
 * per-workspace credentials when those are present.
 */
@ConfigurationProperties(prefix = "projectanalytics.openproject.oauth")
public class OpenProjectOAuthProperties {

    private String clientId = "";
    private String clientSecret = "";
    /**
     * Must match the redirect URI registered in each OpenProject OAuth application.
     * Example: http://localhost:8080/api/v1/workspaces/oauth/callback
     */
    private String redirectUri = "";
    private String scopes = "api_v3";
    /**
     * Where the browser is sent after a successful OAuth callback.
     * Public {@code /oauth/complete} page — must not require a PA JWT in the return window.
     */
    private String frontendSuccessUrl = "http://localhost:4200/oauth/complete?oauth=success";
    /**
     * Where the browser is sent after a failed OAuth callback (append {@code &message=...}).
     */
    private String frontendErrorUrl = "http://localhost:4200/oauth/complete?oauth=error";
    /**
     * Pending state TTL in seconds (default 10 minutes).
     */
    private int stateTtlSeconds = 600;

    /** PA can run OAuth when the callback redirect URI is configured. */
    public boolean isEnabled() {
        return notBlank(redirectUri);
    }

    /** Optional global client credentials for local/single-tenant convenience. */
    public boolean hasGlobalClientDefaults() {
        return notBlank(clientId) && notBlank(clientSecret);
    }

    /** @deprecated use {@link #isEnabled()} — global client is no longer required. */
    public boolean isConfigured() {
        return isEnabled();
    }

    private static boolean notBlank(String value) {
        return value != null && !value.isBlank();
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getRedirectUri() {
        return redirectUri;
    }

    public void setRedirectUri(String redirectUri) {
        this.redirectUri = redirectUri;
    }

    public String getScopes() {
        return scopes;
    }

    public void setScopes(String scopes) {
        this.scopes = scopes;
    }

    public String getFrontendSuccessUrl() {
        return frontendSuccessUrl;
    }

    public void setFrontendSuccessUrl(String frontendSuccessUrl) {
        this.frontendSuccessUrl = frontendSuccessUrl;
    }

    public String getFrontendErrorUrl() {
        return frontendErrorUrl;
    }

    public void setFrontendErrorUrl(String frontendErrorUrl) {
        this.frontendErrorUrl = frontendErrorUrl;
    }

    public int getStateTtlSeconds() {
        return stateTtlSeconds;
    }

    public void setStateTtlSeconds(int stateTtlSeconds) {
        this.stateTtlSeconds = stateTtlSeconds;
    }
}
