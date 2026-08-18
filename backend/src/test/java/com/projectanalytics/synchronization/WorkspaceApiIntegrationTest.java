package com.projectanalytics.synchronization;

import com.projectanalytics.infrastructure.openproject.OpenProjectClient;
import com.projectanalytics.infrastructure.openproject.OpenProjectConnectionProperties;
import com.projectanalytics.infrastructure.openproject.dto.OpenProjectProjectDto;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class WorkspaceApiIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    private OpenProjectClient openProjectClient;

    @MockBean
    private OpenProjectEligibilityService eligibilityService;

    @Test
    @DisplayName("authenticated user can connect via api-key and synchronize")
    void workspaceLifecycle() {
        when(eligibilityService.evaluate(any())).thenReturn(
                new OpenProjectEligibilityService.EligibilityResult(
                        true,
                        1L,
                        "admin",
                        "admin@example.test",
                        true,
                        List.of("Project admin"),
                        "test eligible"
                )
        );
        when(openProjectClient.fetchServerVersion(any(OpenProjectConnectionProperties.class)))
                .thenReturn("14.0.0");
        when(openProjectClient.fetchProjectAdminNamesByProjectId(any(OpenProjectConnectionProperties.class)))
                .thenReturn(java.util.Map.of());
        when(openProjectClient.fetchProjects(any(OpenProjectConnectionProperties.class), isNull()))
                .thenReturn(List.of(
                        new OpenProjectProjectDto(1L, "P1", null, "ACTIVE", null, null, null, null)
                ));
        when(openProjectClient.fetchWorkPackages(any(OpenProjectConnectionProperties.class), any(Long.class), isNull()))
                .thenReturn(List.of());

        String token = login();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        ResponseEntity<Map> createResponse = restTemplate.exchange(
                "/api/v1/workspaces/connect/api-key",
                HttpMethod.POST,
                new HttpEntity<>(
                        Map.of(
                                "name", "Primary OP",
                                "baseUrl", "https://op-workspace-api.test",
                                "apiKey", "test-workspace-key"
                        ),
                        headers
                ),
                Map.class
        );
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        @SuppressWarnings("unchecked")
        Map<String, Object> created = (Map<String, Object>) createResponse.getBody().get("data");
        String workspaceId = (String) created.get("id");

        ResponseEntity<Map> syncResponse = restTemplate.exchange(
                "/api/v1/workspaces/" + workspaceId + "/synchronize",
                HttpMethod.POST,
                new HttpEntity<>(headers),
                Map.class
        );
        assertThat(syncResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        @SuppressWarnings("unchecked")
        Map<String, Object> syncData = (Map<String, Object>) syncResponse.getBody().get("data");
        assertThat(syncData.get("status")).isEqualTo("SUCCESS");

        ResponseEntity<Map> statusResponse = restTemplate.exchange(
                "/api/v1/workspaces/" + workspaceId + "/synchronization",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                Map.class
        );
        assertThat(statusResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        @SuppressWarnings("unchecked")
        Map<String, Object> statusData = (Map<String, Object>) statusResponse.getBody().get("data");
        assertThat(statusData.get("synchronizedProjects")).isEqualTo(1);
    }

    @Test
    @DisplayName("legacy POST /workspaces is disabled")
    void legacyCreateDisabled() {
        String token = login();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        ResponseEntity<Map> response = restTemplate.exchange(
                "/api/v1/workspaces",
                HttpMethod.POST,
                new HttpEntity<>(
                        Map.of("name", "Legacy", "baseUrl", "https://op-legacy-disabled.test"),
                        headers
                ),
                Map.class
        );
        assertThat(response.getStatusCode().is4xxClientError()).isTrue();
        @SuppressWarnings("unchecked")
        Map<String, Object> error = (Map<String, Object>) response.getBody().get("error");
        assertThat(error.get("message").toString()).contains("connect/api-key");
    }

    private String login() {
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
