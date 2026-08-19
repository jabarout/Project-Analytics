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
 * Phase 1 IDOR / isolation checks: unrelated VIEWER cannot access admin workspace analytics.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class WorkspaceIsolationApiIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    private OpenProjectEligibilityService eligibilityService;

    @Autowired
    private TestMailLinkCaptor mailLinkCaptor;

    @Test
    @DisplayName("new viewer with no membership cannot list workspaces or hit explorer for admin workspace")
    void viewerWithoutMembershipIsIsolated() {
        when(eligibilityService.evaluate(any())).thenReturn(
                new OpenProjectEligibilityService.EligibilityResult(
                        true, 1L, "admin", null, true, List.of("Project admin"), "test"
                )
        );

        String adminToken = login("admin", "Admin123!");
        HttpHeaders adminHeaders = bearer(adminToken);

        ResponseEntity<Map> create = restTemplate.exchange(
                "/api/v1/workspaces/connect/api-key",
                HttpMethod.POST,
                new HttpEntity<>(
                        Map.of(
                                "name", "Iso WS",
                                "baseUrl", "https://op-isolation-" + UUID.randomUUID() + ".test",
                                "apiKey", "admin-key"
                        ),
                        adminHeaders
                ),
                Map.class
        );
        assertThat(create.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        @SuppressWarnings("unchecked")
        Map<String, Object> created = (Map<String, Object>) create.getBody().get("data");
        String workspaceId = (String) created.get("id");

        String email = "iso_" + System.nanoTime() + "@example.test";
        String username = "iso_" + System.nanoTime();
        String viewerToken = AuthTestSupport.registerConfirmAndLogin(
                restTemplate, mailLinkCaptor, email, "Welcome123!", username
        );
        HttpHeaders viewerHeaders = bearer(viewerToken);

        ResponseEntity<Map> list = restTemplate.exchange(
                "/api/v1/workspaces",
                HttpMethod.GET,
                new HttpEntity<>(viewerHeaders),
                Map.class
        );
        assertThat(list.getStatusCode()).isEqualTo(HttpStatus.OK);
        @SuppressWarnings("unchecked")
        java.util.List<?> workspaces = (java.util.List<?>) list.getBody().get("data");
        assertThat(workspaces).isEmpty();

        ResponseEntity<Map> explorer = restTemplate.exchange(
                "/api/v1/analytics/workspaces/" + workspaceId + "/explorer-projects",
                HttpMethod.GET,
                new HttpEntity<>(viewerHeaders),
                Map.class
        );
        assertThat(explorer.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

        ResponseEntity<Map> dashboard = restTemplate.exchange(
                "/api/v1/workspaces/" + workspaceId + "/dashboard",
                HttpMethod.GET,
                new HttpEntity<>(viewerHeaders),
                Map.class
        );
        assertThat(dashboard.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

        ResponseEntity<Map> portfolios = restTemplate.exchange(
                "/api/v1/portfolios?workspaceId=" + workspaceId,
                HttpMethod.GET,
                new HttpEntity<>(viewerHeaders),
                Map.class
        );
        assertThat(portfolios.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
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
