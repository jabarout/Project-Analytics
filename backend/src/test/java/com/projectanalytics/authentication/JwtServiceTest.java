package com.projectanalytics.authentication;

import com.projectanalytics.authentication.domain.Role;
import com.projectanalytics.authentication.security.AuthenticatedUser;
import com.projectanalytics.authentication.security.JwtProperties;
import com.projectanalytics.authentication.security.JwtService;
import com.projectanalytics.common.exception.BusinessException;
import com.projectanalytics.common.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        JwtProperties properties = new JwtProperties();
        properties.setSecret("unit-test-jwt-secret-key-32bytes!!");
        properties.setExpirationSeconds(3600);
        jwtService = new JwtService(properties);
    }

    @Test
    void generateAndParseToken_roundTrip() {
        AuthenticatedUser user = new AuthenticatedUser(
                UUID.randomUUID(),
                "admin",
                "hash",
                Role.ADMINISTRATOR,
                true
        );

        String token = jwtService.generateToken(user);
        AuthenticatedUser parsed = jwtService.parseAuthenticatedUser(token);

        assertThat(parsed.getUsername()).isEqualTo("admin");
        assertThat(parsed.getId()).isEqualTo(user.getId());
        assertThat(parsed.getRole()).isEqualTo(Role.ADMINISTRATOR);
        assertThat(jwtService.extractExpiration(token)).isAfter(java.time.Instant.now());
    }

    @Test
    void parseInvalidToken_throwsAuth003() {
        assertThatThrownBy(() -> jwtService.parseAuthenticatedUser("not-a-jwt"))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.AUTH_003);
    }
}
