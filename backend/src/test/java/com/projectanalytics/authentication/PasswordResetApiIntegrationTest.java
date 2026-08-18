package com.projectanalytics.authentication;

import com.projectanalytics.authentication.api.dto.ResetPasswordRequest;
import com.projectanalytics.authentication.application.PasswordResetService;
import com.projectanalytics.authentication.persistence.UserEntity;
import com.projectanalytics.authentication.persistence.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class PasswordResetApiIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordResetService passwordResetService;

    @Test
    @DisplayName("forgot password is enumeration-safe; reset updates password and rejects reuse")
    void forgotAndResetFlow() {
        String email = "reset_" + System.nanoTime() + "@example.test";
        restTemplate.postForEntity(
                "/api/v1/auth/register",
                Map.of("email", email, "password", "Welcome123!", "username", "rst_" + System.nanoTime()),
                Map.class
        );

        ResponseEntity<Map> forgotKnown = restTemplate.postForEntity(
                "/api/v1/auth/forgot-password",
                Map.of("email", email),
                Map.class
        );
        ResponseEntity<Map> forgotUnknown = restTemplate.postForEntity(
                "/api/v1/auth/forgot-password",
                Map.of("email", "nosuch_" + System.nanoTime() + "@example.test"),
                Map.class
        );
        assertThat(forgotKnown.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(forgotUnknown.getStatusCode()).isEqualTo(HttpStatus.OK);
        @SuppressWarnings("unchecked")
        String knownMsg = (String) ((Map<?, ?>) forgotKnown.getBody().get("data")).get("message");
        @SuppressWarnings("unchecked")
        String unknownMsg = (String) ((Map<?, ?>) forgotUnknown.getBody().get("data")).get("message");
        assertThat(knownMsg).isEqualTo(unknownMsg);

        UserEntity user = userRepository.findByEmailIgnoreCase(email).orElseThrow();
        String raw = passwordResetService.issueRawTokenForTests(user.getId());

        passwordResetService.resetPassword(new ResetPasswordRequest(raw, "NewPass123!"));

        ResponseEntity<Map> loginOld = restTemplate.postForEntity(
                "/api/v1/auth/login",
                Map.of("username", email, "password", "Welcome123!"),
                Map.class
        );
        assertThat(loginOld.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

        ResponseEntity<Map> loginNew = restTemplate.postForEntity(
                "/api/v1/auth/login",
                Map.of("username", email, "password", "NewPass123!"),
                Map.class
        );
        assertThat(loginNew.getStatusCode()).isEqualTo(HttpStatus.OK);

        assertThatThrownBy(() -> passwordResetService.resetPassword(new ResetPasswordRequest(raw, "Another123!")))
                .hasMessageContaining("Invalid or expired");
    }
}
