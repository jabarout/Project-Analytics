package com.projectanalytics.security;

import com.projectanalytics.authentication.AuthTestSupport;
import com.projectanalytics.authentication.support.TestMailLinkCaptor;
import com.projectanalytics.synchronization.application.OpenProjectEligibilityService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Phase 2: credential overwrite protection for already-connected OpenProject URLs.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ConnectCredentialHardeningApiIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    private OpenProjectEligibilityService eligibilityService;

    @Autowired
    private TestMailLinkCaptor mailLinkCaptor;

    @Test
    @DisplayName("second eligible user cannot overwrite credentials on an already-connected URL")
    void secondUserCannotOverwriteCredentials() {
        when(eligibilityService.evaluate(any())).thenReturn(
                new OpenProjectEligibilityService.EligibilityResult(
                        true, 9L, "alice", null, false, List.of("Project admin"), "project admin"
                )
        );

        String baseUrl = "https://op-overwrite-" + UUID.randomUUID() + ".test";

        String adminToken = login("admin", "Admin123!");
        HttpHeaders adminHeaders = bearer(adminToken);
        ResponseEntity<Map> first = restTemplate.exchange(
                "/api/v1/workspaces/connect/api-key",
                HttpMethod.POST,
                new HttpEntity<>(
                        Map.of("name", "Owned", "baseUrl", baseUrl, "apiKey", "admin-secret-key"),
                        adminHeaders
                ),
                Map.class
        );
        assertThat(first.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        String email = "attacker_" + System.nanoTime() + "@example.test";
        String username = "att_" + System.nanoTime();
        String attackerToken = AuthTestSupport.registerConfirmAndLogin(
                restTemplate, mailLinkCaptor, email, "Welcome123!", username
        );

        ResponseEntity<Map> second = restTemplate.exchange(
                "/api/v1/workspaces/connect/api-key",
                HttpMethod.POST,
                new HttpEntity<>(
                        Map.of("name", "Hijack", "baseUrl", baseUrl, "apiKey", "attacker-secret-key"),
                        bearer(attackerToken)
                ),
                Map.class
        );
        assertThat(second.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        @SuppressWarnings("unchecked")
        Map<String, Object> error = (Map<String, Object>) second.getBody().get("error");
        assertThat(error.get("code")).isEqualTo("AUTH_006");
        assertThat(error.get("message").toString()).containsIgnoringCase("already connected");
    }

    private String login(String username, String password) {
        ResponseEntity<Map> response = restTemplate.postForEntity(
                "/api/v1/auth/login",
                Map.of("username", username, "password", password),
                Map.class
        );
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
        return (String) data.get("token");
    }

    private static HttpHeaders bearer(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return headers;
    }
}
