package com.projectanalytics.authentication.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Fail-fast production checks so insecure defaults never run in prod (M10).
 */
@Component
@Profile("prod")
@Order(0)
public class ProductionSecurityValidator implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(ProductionSecurityValidator.class);

    private static final String DEV_DEFAULT_SECRET = "project-analytics-dev-jwt-secret-change-me-32b";
    private static final int MIN_SECRET_LENGTH = 32;

    private final JwtProperties jwtProperties;
    private final String corsAllowedOrigins;
    private final String credentialsEncryptionKey;

    public ProductionSecurityValidator(
            JwtProperties jwtProperties,
            @Value("${projectanalytics.cors.allowed-origins:}") String corsAllowedOrigins,
            @Value("${projectanalytics.security.credentials-encryption-key:}") String credentialsEncryptionKey
    ) {
        this.jwtProperties = jwtProperties;
        this.corsAllowedOrigins = corsAllowedOrigins == null ? "" : corsAllowedOrigins;
        this.credentialsEncryptionKey = credentialsEncryptionKey == null ? "" : credentialsEncryptionKey;
    }

    @Override
    public void run(ApplicationArguments args) {
        String secret = jwtProperties.getSecret();
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException(
                    "JWT_SECRET / projectanalytics.jwt.secret must be set in the prod profile."
            );
        }
        if (secret.equals(DEV_DEFAULT_SECRET)) {
            throw new IllegalStateException(
                    "JWT_SECRET must not use the development default secret in the prod profile."
            );
        }
        if (secret.length() < MIN_SECRET_LENGTH) {
            throw new IllegalStateException(
                    "JWT_SECRET must be at least " + MIN_SECRET_LENGTH + " characters in the prod profile."
            );
        }
        if (corsAllowedOrigins.isBlank() || corsAllowedOrigins.contains("*")) {
            throw new IllegalStateException(
                    "CORS_ALLOWED_ORIGINS must be an explicit non-wildcard origin list in the prod profile."
            );
        }
        if (corsAllowedOrigins.contains("localhost") || corsAllowedOrigins.contains("127.0.0.1")) {
            log.warn(
                    "CORS allowed origins include localhost in prod — verify this is intentional for this environment."
            );
        }
        if (credentialsEncryptionKey.isBlank()) {
            throw new IllegalStateException(
                    "CREDENTIALS_ENCRYPTION_KEY / projectanalytics.security.credentials-encryption-key "
                            + "must be set in the prod profile (do not fall back to JWT_SECRET)."
            );
        }
        if (credentialsEncryptionKey.length() < MIN_SECRET_LENGTH) {
            throw new IllegalStateException(
                    "CREDENTIALS_ENCRYPTION_KEY must be at least " + MIN_SECRET_LENGTH + " characters in prod."
            );
        }
        if (credentialsEncryptionKey.equals(secret)) {
            throw new IllegalStateException(
                    "CREDENTIALS_ENCRYPTION_KEY must be distinct from JWT_SECRET in the prod profile."
            );
        }
        log.info("Production security startup checks passed.");
    }
}
