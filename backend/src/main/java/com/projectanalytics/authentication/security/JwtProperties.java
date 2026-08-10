package com.projectanalytics.authentication.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Externalized JWT configuration (never hardcode secrets).
 */
@ConfigurationProperties(prefix = "projectanalytics.jwt")
public class JwtProperties {

    /**
     * HMAC signing secret. Must be sufficiently long for HS256.
     */
    private String secret;

    /**
     * Access token lifetime in seconds.
     */
    private long expirationSeconds = 3600;

    /**
     * Reserved for future refresh-token support (seconds).
     */
    private long refreshExpirationSeconds = 86400;

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public long getExpirationSeconds() {
        return expirationSeconds;
    }

    public void setExpirationSeconds(long expirationSeconds) {
        this.expirationSeconds = expirationSeconds;
    }

    public long getRefreshExpirationSeconds() {
        return refreshExpirationSeconds;
    }

    public void setRefreshExpirationSeconds(long refreshExpirationSeconds) {
        this.refreshExpirationSeconds = refreshExpirationSeconds;
    }
}
