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
    @DisplayName("register creates VIEWER account and returns token")
    void registerSucceeds() {
        String email = "newbie_" + System.nanoTime() + "@example.test";
        ResponseEntity<Map> response = restTemplate.postForEntity(
                "/api/v1/auth/register",
                Map.of(
                        "email", email,
                        "password", "Welcome123!",
                        "username", "newbie_" + System.nanoTime()
                ),
                Map.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("success")).isEqualTo(true);

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
        assertThat(data.get("token")).asString().isNotBlank();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth((String) data.get("token"));
        ResponseEntity<Map> meResponse = restTemplate.exchange(
                "/api/v1/auth/me",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                Map.class
        );
        assertThat(meResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        @SuppressWarnings("unchecked")
        Map<String, Object> meData = (Map<String, Object>) meResponse.getBody().get("data");
        assertThat(meData.get("email")).isEqualTo(email);
        assertThat(meData.get("role")).isEqualTo("VIEWER");
        assertThat(meData.get("role")).isNotEqualTo("ADMINISTRATOR");
    }

    @Test
    @DisplayName("register rejects duplicate email")
    void registerRejectsDuplicateEmail() {
        String email = "dup_" + System.nanoTime() + "@example.test";
        Map<String, Object> body = Map.of(
                "email", email,
                "password", "Welcome123!",
                "username", "dupuser_" + System.nanoTime()
        );
        assertThat(restTemplate.postForEntity("/api/v1/auth/register", body, Map.class).getStatusCode())
                .isEqualTo(HttpStatus.OK);

        ResponseEntity<Map> second = restTemplate.postForEntity(
                "/api/v1/auth/register",
                Map.of(
                        "email", email,
                        "password", "Welcome123!",
                        "username", "other_" + System.nanoTime()
                ),
                Map.class
        );
        assertThat(second.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        @SuppressWarnings("unchecked")
        Map<String, Object> error = (Map<String, Object>) second.getBody().get("error");
        assertThat(error.get("code")).isEqualTo("USER_002");
    }

    @Test
    @DisplayName("login accepts email as well as username")
    void loginWithEmail() {
        ResponseEntity<Map> response = restTemplate.postForEntity(
                "/api/v1/auth/login",
                Map.of("username", "admin@projectanalytics.local", "password", "Admin123!"),
                Map.class
        );
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
        assertThat(data.get("token")).asString().isNotBlank();
    }

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
