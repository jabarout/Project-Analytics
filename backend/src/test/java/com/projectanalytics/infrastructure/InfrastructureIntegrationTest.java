package com.projectanalytics.infrastructure;

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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Infrastructure integration tests for foundation + authentication wiring.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class InfrastructureIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("application context starts with test infrastructure profile")
    void applicationStartsSuccessfully() {
        assertThat(restTemplate).isNotNull();
        assertThat(jdbcTemplate).isNotNull();
    }

    @Test
    @DisplayName("actuator health endpoint reports UP")
    void healthEndpointIsUp() {
        ResponseEntity<Map> response = restTemplate.getForEntity("/actuator/health", Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo("UP");
    }

    @Test
    @DisplayName("OpenAPI document is published")
    void openApiDocumentIsAvailable() {
        ResponseEntity<Map> response = restTemplate.getForEntity("/v3/api-docs", Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("openapi")).asString().startsWith("3.");
        assertThat(response.getBody()).containsKey("paths");
        assertThat(response.getBody().get("info")).isInstanceOf(Map.class);

        @SuppressWarnings("unchecked")
        Map<String, Object> info = (Map<String, Object>) response.getBody().get("info");
        assertThat(info.get("title")).isEqualTo("Project Analytics API");
    }

    @Test
    @DisplayName("Flyway migrations applied foundation and user schema")
    void flywayMigrationsApplied() {
        Integer foundationCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM schema_foundation WHERE id = 1",
                Integer.class
        );
        Integer userCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM users WHERE username = 'admin'",
                Integer.class
        );
        Integer flywayV2 = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM flyway_schema_history WHERE success = true AND version = '2'",
                Integer.class
        );

        assertThat(foundationCount).isEqualTo(1);
        assertThat(userCount).isEqualTo(1);
        assertThat(flywayV2).isEqualTo(1);
    }

    @Test
    @DisplayName("system info endpoint returns standard API envelope when authenticated")
    void systemInfoEndpointReturnsEnvelope() {
        String token = loginAndGetToken();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        ResponseEntity<Map> response = restTemplate.exchange(
                "/api/v1/system/info",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                Map.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("success")).isEqualTo(true);
        assertThat(response.getBody().get("data")).isInstanceOf(Map.class);
        assertThat(response.getBody().get("timestamp")).isNotNull();

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
        assertThat(data.get("application")).isEqualTo("project-analytics-backend");
        assertThat(data.get("apiVersion")).isEqualTo("v1");
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
