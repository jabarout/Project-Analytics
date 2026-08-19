package com.projectanalytics.authentication;

import com.projectanalytics.authentication.support.TestMailLinkCaptor;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Shared helpers for auth integration tests after email-confirmation-on-signup.
 */
public final class AuthTestSupport {

    private AuthTestSupport() {
    }

    /**
     * Registers a user and confirms email using the token captured in the test profile.
     * Returns login credentials map (username + password) — caller logs in for a JWT.
     */
    public static void registerAndConfirm(
            TestRestTemplate restTemplate,
            TestMailLinkCaptor mailLinkCaptor,
            String email,
            String password,
            String username
    ) {
        mailLinkCaptor.clear();
        ResponseEntity<Map> register = restTemplate.postForEntity(
                "/api/v1/auth/register",
                Map.of("email", email, "password", password, "username", username),
                Map.class
        );
        assertThat(register.getStatusCode()).isEqualTo(HttpStatus.OK);
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) register.getBody().get("data");
        assertThat(data.get("token")).isNull();
        assertThat(data.get("message").toString()).containsIgnoringCase("confirm");

        String rawToken = mailLinkCaptor.lastConfirmationToken();
        assertThat(rawToken).as("confirmation token should be captured in test profile").isNotBlank();

        ResponseEntity<Map> confirm = restTemplate.postForEntity(
                "/api/v1/auth/confirm-email",
                Map.of("token", rawToken),
                Map.class
        );
        assertThat(confirm.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    public static String registerConfirmAndLogin(
            TestRestTemplate restTemplate,
            TestMailLinkCaptor mailLinkCaptor,
            String email,
            String password,
            String username
    ) {
        registerAndConfirm(restTemplate, mailLinkCaptor, email, password, username);
        ResponseEntity<Map> login = restTemplate.postForEntity(
                "/api/v1/auth/login",
                Map.of("username", username, "password", password),
                Map.class
        );
        assertThat(login.getStatusCode()).isEqualTo(HttpStatus.OK);
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) login.getBody().get("data");
        return (String) data.get("token");
    }
}
