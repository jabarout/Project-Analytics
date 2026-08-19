package com.projectanalytics.authentication;

import com.projectanalytics.authentication.support.TestMailLinkCaptor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * API-level authentication tests for Milestone 2 + email confirmation.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class AuthenticationApiIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private TestMailLinkCaptor mailLinkCaptor;

    @Test
    @DisplayName("register requires email confirmation before login and token")
    void registerRequiresEmailConfirmation() {
        String email = "newbie_" + System.nanoTime() + "@example.test";
        String username = "newbie_" + System.nanoTime();
        mailLinkCaptor.clear();

        ResponseEntity<Map> response = restTemplate.postForEntity(
                "/api/v1/auth/register",
                Map.of("email", email, "password", "Welcome123!", "username", username),
                Map.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("success")).isEqualTo(true);
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
        assertThat(data.get("token")).isNull();
        assertThat(data.get("message").toString()).containsIgnoringCase("confirm");

        ResponseEntity<Map> blocked = restTemplate.postForEntity(
                "/api/v1/auth/login",
                Map.of("username", username, "password", "Welcome123!"),
                Map.class
        );
        assertThat(blocked.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        @SuppressWarnings("unchecked")
        Map<String, Object> blockedErr = (Map<String, Object>) blocked.getBody().get("error");
        assertThat(blockedErr.get("code")).isEqualTo("AUTH_008");

        String rawToken = mailLinkCaptor.lastConfirmationToken();
        assertThat(rawToken).isNotBlank();
        ResponseEntity<Map> confirm = restTemplate.postForEntity(
                "/api/v1/auth/confirm-email",
                Map.of("token", rawToken),
                Map.class
        );
        assertThat(confirm.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<Map> login = restTemplate.postForEntity(
                "/api/v1/auth/login",
                Map.of("username", username, "password", "Welcome123!"),
                Map.class
        );
        assertThat(login.getStatusCode()).isEqualTo(HttpStatus.OK);
        @SuppressWarnings("unchecked")
        Map<String, Object> loginData = (Map<String, Object>) login.getBody().get("data");
        assertThat(loginData.get("token")).asString().isNotBlank();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth((String) loginData.get("token"));
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
        String username = "dupuser_" + System.nanoTime();
        AuthTestSupport.registerAndConfirm(restTemplate, mailLinkCaptor, email, "Welcome123!", username);

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
}
