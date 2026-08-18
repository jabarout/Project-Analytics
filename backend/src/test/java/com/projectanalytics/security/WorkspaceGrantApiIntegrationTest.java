package com.projectanalytics.security;

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
 * Phase 6 / M15: Workspace Admin grants analytics access; grantee is isolated until granted;
 * revoke restores isolation; non-admins cannot grant; Workspace Admin cannot be revoked via grants.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class WorkspaceGrantApiIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    private OpenProjectEligibilityService eligibilityService;

    @Test
    @DisplayName("grant matrix: admin grants → viewer sees workspace; revoke → isolated; admin protected")
    void grantRevokeMatrix() {
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
                                "name", "Grant WS",
                                "baseUrl", "https://op-grant-" + UUID.randomUUID() + ".test",
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
        assertThat(created.get("workspaceAdmin")).isEqualTo(true);

        String email = "grant_" + System.nanoTime() + "@example.test";
        String username = "grant_" + System.nanoTime();
        ResponseEntity<Map> register = restTemplate.postForEntity(
                "/api/v1/auth/register",
                Map.of("email", email, "password", "Welcome123!", "username", username),
                Map.class
        );
        assertThat(register.getStatusCode()).isEqualTo(HttpStatus.OK);
        @SuppressWarnings("unchecked")
        Map<String, Object> regData = (Map<String, Object>) register.getBody().get("data");
        String viewerToken = (String) regData.get("token");
        HttpHeaders viewerHeaders = bearer(viewerToken);

        // Before grant: isolated
        assertThat(listWorkspaceIds(viewerHeaders)).isEmpty();
        assertThat(getDashboard(workspaceId, viewerHeaders).getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

        // Viewer cannot grant
        ResponseEntity<Map> viewerGrant = restTemplate.exchange(
                "/api/v1/workspaces/" + workspaceId + "/members",
                HttpMethod.POST,
                new HttpEntity<>(Map.of("email", "anyone@example.test"), viewerHeaders),
                Map.class
        );
        assertThat(viewerGrant.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

        // Admin grants
        ResponseEntity<Map> grant = restTemplate.exchange(
                "/api/v1/workspaces/" + workspaceId + "/members",
                HttpMethod.POST,
                new HttpEntity<>(Map.of("email", email), adminHeaders),
                Map.class
        );
        assertThat(grant.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        @SuppressWarnings("unchecked")
        Map<String, Object> granted = (Map<String, Object>) grant.getBody().get("data");
        String viewerUserId = (String) granted.get("userId");
        assertThat(granted.get("analyticsAccess")).isEqualTo(true);
        assertThat(granted.get("workspaceAdmin")).isEqualTo(false);

        // After grant: viewer sees workspace + dashboard OK
        assertThat(listWorkspaceIds(viewerHeaders)).contains(workspaceId);
        assertThat(getDashboard(workspaceId, viewerHeaders).getStatusCode()).isEqualTo(HttpStatus.OK);

        // Members list (admin only)
        ResponseEntity<Map> members = restTemplate.exchange(
                "/api/v1/workspaces/" + workspaceId + "/members",
                HttpMethod.GET,
                new HttpEntity<>(adminHeaders),
                Map.class
        );
        assertThat(members.getStatusCode()).isEqualTo(HttpStatus.OK);
        @SuppressWarnings("unchecked")
        List<?> memberList = (List<?>) members.getBody().get("data");
        assertThat(memberList).hasSizeGreaterThanOrEqualTo(2);

        // Viewer cannot list members
        ResponseEntity<Map> viewerMembers = restTemplate.exchange(
                "/api/v1/workspaces/" + workspaceId + "/members",
                HttpMethod.GET,
                new HttpEntity<>(viewerHeaders),
                Map.class
        );
        assertThat(viewerMembers.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

        // Cannot revoke Workspace Admin via grants
        String adminUserId = memberList.stream()
                .map(row -> (Map<?, ?>) row)
                .filter(row -> Boolean.TRUE.equals(row.get("workspaceAdmin")))
                .map(row -> (String) row.get("userId"))
                .findFirst()
                .orElseThrow();
        ResponseEntity<Map> revokeAdmin = restTemplate.exchange(
                "/api/v1/workspaces/" + workspaceId + "/members/" + adminUserId,
                HttpMethod.DELETE,
                new HttpEntity<>(adminHeaders),
                Map.class
        );
        assertThat(revokeAdmin.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

        // Revoke viewer → isolated again
        ResponseEntity<Void> revoke = restTemplate.exchange(
                "/api/v1/workspaces/" + workspaceId + "/members/" + viewerUserId,
                HttpMethod.DELETE,
                new HttpEntity<>(adminHeaders),
                Void.class
        );
        assertThat(revoke.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(listWorkspaceIds(viewerHeaders)).doesNotContain(workspaceId);
        assertThat(getDashboard(workspaceId, viewerHeaders).getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

        // Unknown email → USER_001
        ResponseEntity<Map> missing = restTemplate.exchange(
                "/api/v1/workspaces/" + workspaceId + "/members",
                HttpMethod.POST,
                new HttpEntity<>(Map.of("email", "missing_" + System.nanoTime() + "@example.test"), adminHeaders),
                Map.class
        );
        assertThat(missing.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        @SuppressWarnings("unchecked")
        Map<String, Object> missingErr = (Map<String, Object>) missing.getBody().get("error");
        assertThat(missingErr.get("code")).isEqualTo("USER_001");
    }

    private List<String> listWorkspaceIds(HttpHeaders headers) {
        ResponseEntity<Map> list = restTemplate.exchange(
                "/api/v1/workspaces",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                Map.class
        );
        assertThat(list.getStatusCode()).isEqualTo(HttpStatus.OK);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> workspaces = (List<Map<String, Object>>) list.getBody().get("data");
        return workspaces.stream().map(row -> (String) row.get("id")).toList();
    }

    private ResponseEntity<Map> getDashboard(String workspaceId, HttpHeaders headers) {
        return restTemplate.exchange(
                "/api/v1/workspaces/" + workspaceId + "/dashboard",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                Map.class
        );
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
