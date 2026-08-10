package com.projectanalytics.authentication.security;

import org.junit.jupiter.api.Test;
import org.springframework.boot.DefaultApplicationArguments;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductionSecurityValidatorTest {

    @Test
    void rejectsDevDefaultSecret() {
        JwtProperties jwt = new JwtProperties();
        jwt.setSecret("project-analytics-dev-jwt-secret-change-me-32b");
        ProductionSecurityValidator validator =
                new ProductionSecurityValidator(jwt, "https://app.example.com");

        assertThatThrownBy(() -> validator.run(new DefaultApplicationArguments()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("development default");
    }

    @Test
    void rejectsShortSecret() {
        JwtProperties jwt = new JwtProperties();
        jwt.setSecret("too-short-secret");
        ProductionSecurityValidator validator =
                new ProductionSecurityValidator(jwt, "https://app.example.com");

        assertThatThrownBy(() -> validator.run(new DefaultApplicationArguments()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("32");
    }

    @Test
    void rejectsWildcardCors() {
        JwtProperties jwt = new JwtProperties();
        jwt.setSecret("production-grade-jwt-secret-value-32b");
        ProductionSecurityValidator validator = new ProductionSecurityValidator(jwt, "*");

        assertThatThrownBy(() -> validator.run(new DefaultApplicationArguments()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("CORS");
    }

    @Test
    void acceptsStrongSecretAndExplicitCors() {
        JwtProperties jwt = new JwtProperties();
        jwt.setSecret("production-grade-jwt-secret-value-32b");
        ProductionSecurityValidator validator =
                new ProductionSecurityValidator(jwt, "https://analytics.example.com");

        assertThatCode(() -> validator.run(new DefaultApplicationArguments())).doesNotThrowAnyException();
    }
}
