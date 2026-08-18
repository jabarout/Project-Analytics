package com.projectanalytics.authentication.security;

import org.junit.jupiter.api.Test;
import org.springframework.boot.DefaultApplicationArguments;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductionSecurityValidatorTest {

    private static final String STRONG_JWT = "production-grade-jwt-secret-value-32b";
    private static final String STRONG_CRED = "production-grade-cred-secret-value-32b";
    private static final String CORS = "https://analytics.example.com";

    @Test
    void rejectsDevDefaultSecret() {
        JwtProperties jwt = new JwtProperties();
        jwt.setSecret("project-analytics-dev-jwt-secret-change-me-32b");
        ProductionSecurityValidator validator =
                new ProductionSecurityValidator(jwt, CORS, STRONG_CRED);

        assertThatThrownBy(() -> validator.run(new DefaultApplicationArguments()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("development default");
    }

    @Test
    void rejectsShortSecret() {
        JwtProperties jwt = new JwtProperties();
        jwt.setSecret("too-short-secret");
        ProductionSecurityValidator validator =
                new ProductionSecurityValidator(jwt, CORS, STRONG_CRED);

        assertThatThrownBy(() -> validator.run(new DefaultApplicationArguments()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("32");
    }

    @Test
    void rejectsWildcardCors() {
        JwtProperties jwt = new JwtProperties();
        jwt.setSecret(STRONG_JWT);
        ProductionSecurityValidator validator = new ProductionSecurityValidator(jwt, "*", STRONG_CRED);

        assertThatThrownBy(() -> validator.run(new DefaultApplicationArguments()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("CORS");
    }

    @Test
    void rejectsMissingCredentialsEncryptionKey() {
        JwtProperties jwt = new JwtProperties();
        jwt.setSecret(STRONG_JWT);
        ProductionSecurityValidator validator = new ProductionSecurityValidator(jwt, CORS, "");

        assertThatThrownBy(() -> validator.run(new DefaultApplicationArguments()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("CREDENTIALS_ENCRYPTION_KEY");
    }

    @Test
    void rejectsCredentialsKeyEqualToJwt() {
        JwtProperties jwt = new JwtProperties();
        jwt.setSecret(STRONG_JWT);
        ProductionSecurityValidator validator = new ProductionSecurityValidator(jwt, CORS, STRONG_JWT);

        assertThatThrownBy(() -> validator.run(new DefaultApplicationArguments()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("distinct");
    }

    @Test
    void acceptsStrongSecretAndExplicitCors() {
        JwtProperties jwt = new JwtProperties();
        jwt.setSecret(STRONG_JWT);
        ProductionSecurityValidator validator =
                new ProductionSecurityValidator(jwt, CORS, STRONG_CRED);

        assertThatCode(() -> validator.run(new DefaultApplicationArguments())).doesNotThrowAnyException();
    }
}
