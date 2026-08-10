package com.projectanalytics.authentication;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * API-level authentication tests for Milestone 2.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class AuthenticationApiIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    @DisplayName("login succeeds for seed administrator")
    void loginSucceeds() {
        ResponseEntity<Map> response = restTemplate.postForEntity(
                "/api/v1/auth/login",
                Map.of("username", "admin", "password", "Admin123!"),
                Map.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("success")).isEqualTo(true);

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
        assertThat(data.get("token")).asString().isNotBlank();
        assertThat(data.get("expiresAt")).isNotNull();
    }

    @Test
    @DisplayName("login fails with invalid credentials")
    void loginFailsWithInvalidCredentials() {
        ResponseEntity<Map> response = restTemplate.postForEntity(
                "/api/v1/auth/login",
                Map.of("username", "admin", "password", "WrongPassword1!"),
                Map.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("success")).isEqualTo(false);

        @SuppressWarnings("unchecked")
        Map<String, Object> error = (Map<String, Object>) response.getBody().get("error");
        assertThat(error.get("code")).isEqualTo("AUTH_001");
    }

    @Test
    @DisplayName("protected endpoint rejects missing token")
    void protectedEndpointRequiresAuthentication() {
        ResponseEntity<Map> response = restTemplate.getForEntity("/api/v1/auth/me", Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();

        @SuppressWarnings("unchecked")
        Map<String, Object> error = (Map<String, Object>) response.getBody().get("error");
        assertThat(error.get("code")).isEqualTo("AUTH_004");
    }

    @Test
    @DisplayName("auth me and preferences update with valid token")
    void authenticatedUserFlow() {
        String token = loginAndGetToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<Map> meResponse = restTemplate.exchange(
                "/api/v1/auth/me",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                Map.class
        );
        assertThat(meResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        @SuppressWarnings("unchecked")
        Map<String, Object> meData = (Map<String, Object>) meResponse.getBody().get("data");
        assertThat(meData.get("username")).isEqualTo("admin");
        assertThat(meData.get("role")).isEqualTo("ADMINISTRATOR");

        ResponseEntity<Map> preferencesResponse = restTemplate.exchange(
                "/api/v1/users/me/preferences",
                HttpMethod.PUT,
                new HttpEntity<>(
                        Map.of(
                                "theme", "dark",
                                "language", "en",
                                "dashboardConfiguration", "{\"layout\":\"compact\"}"
                        ),
                        headers
                ),
                Map.class
        );
        assertThat(preferencesResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        @SuppressWarnings("unchecked")
        Map<String, Object> prefData = (Map<String, Object>) preferencesResponse.getBody().get("data");
        assertThat(prefData.get("theme")).isEqualTo("dark");

        ResponseEntity<Map> themeResponse = restTemplate.exchange(
                "/api/v1/users/me/theme",
                HttpMethod.PATCH,
                new HttpEntity<>(Map.of("theme", "light"), headers),
                Map.class
        );
        assertThat(themeResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        @SuppressWarnings("unchecked")
        Map<String, Object> themeData = (Map<String, Object>) themeResponse.getBody().get("data");
        assertThat(themeData.get("theme")).isEqualTo("light");

        ResponseEntity<Map> logoutResponse = restTemplate.exchange(
                "/api/v1/auth/logout",
                HttpMethod.POST,
                new HttpEntity<>(headers),
                Map.class
        );
        assertThat(logoutResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    private String loginAndGetToken() {
        ResponseEntity<Map> response = restTemplate.postForEntity(
                "/api/v1/auth/login",
                Map.of("username", "admin", "password", "Admin123!"),
                Map.class
        );
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
        return (String) data.get("token");
    }
}
