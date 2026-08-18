package com.projectanalytics.authentication;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "projectanalytics.security.rate-limit.enabled=true",
        "projectanalytics.security.rate-limit.login-max-attempts=3",
        "projectanalytics.security.rate-limit.window-seconds=300"
})
class AuthRateLimitApiIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    @DisplayName("login is rate-limited after max attempts in the window")
    void loginRateLimited() {
        ResponseEntity<Map> first = restTemplate.postForEntity(
                "/api/v1/auth/login",
                Map.of("username", "admin", "password", "WrongPassword1!"),
                Map.class
        );
        ResponseEntity<Map> second = restTemplate.postForEntity(
                "/api/v1/auth/login",
                Map.of("username", "admin", "password", "WrongPassword1!"),
                Map.class
        );
        ResponseEntity<Map> third = restTemplate.postForEntity(
                "/api/v1/auth/login",
                Map.of("username", "admin", "password", "WrongPassword1!"),
                Map.class
        );
        ResponseEntity<Map> fourth = restTemplate.postForEntity(
                "/api/v1/auth/login",
                Map.of("username", "admin", "password", "WrongPassword1!"),
                Map.class
        );

        assertThat(first.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(second.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(third.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(fourth.getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
        @SuppressWarnings("unchecked")
        Map<String, Object> error = (Map<String, Object>) fourth.getBody().get("error");
        assertThat(error.get("code")).isEqualTo("AUTH_007");
    }
}
