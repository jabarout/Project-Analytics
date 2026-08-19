package com.projectanalytics.security;

import com.projectanalytics.authentication.AuthTestSupport;
import com.projectanalytics.authentication.support.TestMailLinkCaptor;
import com.projectanalytics.infrastructure.openproject.OpenProjectOAuthClient;
import com.projectanalytics.infrastructure.openproject.OpenProjectOAuthClientCredentials;
import com.projectanalytics.infrastructure.openproject.OpenProjectOAuthTokenResponse;
import com.projectanalytics.infrastructure.security.SecretEncryptionService;
import com.projectanalytics.synchronization.application.OpenProjectEligibilityService;
import com.projectanalytics.synchronization.persistence.WorkspaceCredentialRepository;
import com.projectanalytics.synchronization.persistence.WorkspaceRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * OAuth connect: per-workspace client credentials, eligibility, Hybrid already-connected rule.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestPropertySource(properties = {
        // Redirect only — no global client defaults (forces per-connect client id/secret).
        "projectanalytics.openproject.oauth.client-id=",
        "projectanalytics.openproject.oauth.client-secret=",
        "projectanalytics.openproject.oauth.redirect-uri=http://localhost/api/v1/workspaces/oauth/callback",
        "projectanalytics.openproject.oauth.frontend-success-url=http://localhost:4200/workspaces?oauth=success",
        "projectanalytics.openproject.oauth.frontend-error-url=http://localhost:4200/workspaces?oauth=error"
})
class WorkspaceOAuthConnectApiIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @LocalServerPort
    private int port;

    @Autowired
    private WorkspaceCredentialRepository credentialRepository;

    @Autowired
    private WorkspaceRepository workspaceRepository;

    @Autowired
    private SecretEncryptionService secretEncryptionService;

    @MockBean
    private OpenProjectEligibilityService eligibilityService;

    @MockBean
    private OpenProjectOAuthClient oauthClient;

    @Autowired
    private TestMailLinkCaptor mailLinkCaptor;

    @Test
    @DisplayName("oauth status enabled with redirect URI even without global client defaults")
    void oauthStatusEnabledWithoutGlobalClient() {
        String token = login("admin", "Admin123!");
        ResponseEntity<Map> response = restTemplate.exchange(
                "/api/v1/workspaces/oauth/status",
                HttpMethod.GET,
                new HttpEntity<>(bearer(token)),
                Map.class
        );
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
        assertThat(data.get("enabled")).isEqualTo(true);
        assertThat(data.get("globalClientDefaultsAvailable")).isEqualTo(false);
        assertThat(data.get("redirectUri")).isNotNull();
        assertThat(response.getBody().toString()).doesNotContain("clientSecret");
        assertThat(response.getBody().toString()).doesNotContain("client-secret");
    }

    @Test
    @DisplayName("oauth start → callback stores bearer + per-workspace client credentials")
    void oauthHappyPathStoresPerWorkspaceClient() {
        when(eligibilityService.evaluate(any())).thenReturn(
                new OpenProjectEligibilityService.EligibilityResult(
                        true, 42L, "op-admin", "admin@op.test", true, List.of("Project admin"), "eligible"
                )
        );
        when(oauthClient.exchangeAuthorizationCode(anyString(), anyString(), anyString(), any()))
                .thenReturn(new OpenProjectOAuthTokenResponse(
                        "access-token-1",
                        "refresh-token-1",
                        "Bearer",
                        "api_v3",
                        Instant.now().plusSeconds(7200)
                ));

        String token = login("admin", "Admin123!");
        HttpHeaders headers = bearer(token);
        String baseUrl = "https://op-oauth-" + UUID.randomUUID() + ".test";

        ResponseEntity<Map> start = restTemplate.exchange(
                "/api/v1/workspaces/oauth/start",
                HttpMethod.POST,
                new HttpEntity<>(
                        Map.of(
                                "baseUrl", baseUrl,
                                "name", "OAuth WS",
                                "clientId", "client-a",
                                "clientSecret", "secret-a-value"
                        ),
                        headers
                ),
                Map.class
        );
        assertThat(start.getStatusCode()).isEqualTo(HttpStatus.OK);
        @SuppressWarnings("unchecked")
        Map<String, Object> startData = (Map<String, Object>) start.getBody().get("data");
        String state = (String) startData.get("state");
        String authorizationUrl = (String) startData.get("authorizationUrl");
        assertThat(authorizationUrl).contains("client_id=client-a");
        assertThat(authorizationUrl).doesNotContain("secret-a");

        ResponseEntity<Void> callback = getCallbackNoFollow(
                "/api/v1/workspaces/oauth/callback?code=auth-code-1&state=" + state
        );
        assertThat(callback.getStatusCode()).isEqualTo(HttpStatus.FOUND);
        assertThat(callback.getHeaders().getLocation().toString()).contains("oauth=success");

        ArgumentCaptor<OpenProjectOAuthClientCredentials> clientCaptor =
                ArgumentCaptor.forClass(OpenProjectOAuthClientCredentials.class);
        verify(oauthClient).exchangeAuthorizationCode(anyString(), anyString(), anyString(), clientCaptor.capture());
        assertThat(clientCaptor.getValue().clientId()).isEqualTo("client-a");
        assertThat(clientCaptor.getValue().clientSecret()).isEqualTo("secret-a-value");

        UUID workspaceId = workspaceRepository.findByBaseUrlIgnoreCase(baseUrl).orElseThrow().getId();
        var credential = credentialRepository.findByWorkspaceId(workspaceId).orElseThrow();
        assertThat(credential.getAuthScheme()).isEqualTo("BEARER_TOKEN");
        assertThat(credential.getOauthClientId()).isEqualTo("client-a");
        assertThat(credential.getOauthClientSecretCiphertext()).isNotBlank();
        assertThat(credential.getOauthClientSecretCiphertext()).doesNotContain("secret-a-value");
        assertThat(secretEncryptionService.decrypt(credential.getOauthClientSecretCiphertext()))
                .isEqualTo("secret-a-value");

        ResponseEntity<Map> list = restTemplate.exchange(
                "/api/v1/workspaces",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                Map.class
        );
        String body = String.valueOf(list.getBody());
        assertThat(body).doesNotContain("secret-a-value");
        assertThat(body).doesNotContain("access-token-1");
        assertThat(body).doesNotContain("refresh-token-1");
        assertThat(body).doesNotContain("oauthClientSecret");
    }

    @Test
    @DisplayName("Company A and Company B connect simultaneously with distinct client credentials")
    void multiOpenProjectDistinctClients() {
        when(eligibilityService.evaluate(any())).thenReturn(
                new OpenProjectEligibilityService.EligibilityResult(
                        true, 1L, "admin", null, true, List.of("Project admin"), "eligible"
                )
        );
        when(oauthClient.exchangeAuthorizationCode(anyString(), anyString(), anyString(), any()))
                .thenAnswer(inv -> {
                    OpenProjectOAuthClientCredentials client = inv.getArgument(3);
                    return new OpenProjectOAuthTokenResponse(
                            "access-" + client.clientId(),
                            "refresh-" + client.clientId(),
                            "Bearer",
                            "api_v3",
                            Instant.now().plusSeconds(7200)
                    );
                });

        String adminToken = login("admin", "Admin123!");
        String urlA = "https://op-a-" + UUID.randomUUID() + ".test";
        String urlB = "https://op-b-" + UUID.randomUUID() + ".test";

        // Company A via seed admin
        oauthConnect(adminToken, urlA, "Company A", "client-A", "secret-A-only");
        // Company B via second PA user
        String email = "companyb_" + System.nanoTime() + "@example.test";
        String usernameB = "b_" + System.nanoTime();
        String tokenB = AuthTestSupport.registerConfirmAndLogin(
                restTemplate, mailLinkCaptor, email, "Welcome123!", usernameB
        );
        oauthConnect(tokenB, urlB, "Company B", "client-B", "secret-B-only");

        assertThat(workspaceRepository.findByBaseUrlIgnoreCase(urlA)).isPresent();
        assertThat(workspaceRepository.findByBaseUrlIgnoreCase(urlB)).isPresent();

        var credA = credentialRepository.findByWorkspaceId(
                workspaceRepository.findByBaseUrlIgnoreCase(urlA).orElseThrow().getId()
        ).orElseThrow();
        var credB = credentialRepository.findByWorkspaceId(
                workspaceRepository.findByBaseUrlIgnoreCase(urlB).orElseThrow().getId()
        ).orElseThrow();

        assertThat(credA.getOauthClientId()).isEqualTo("client-A");
        assertThat(credB.getOauthClientId()).isEqualTo("client-B");
        assertThat(secretEncryptionService.decrypt(credA.getOauthClientSecretCiphertext()))
                .isEqualTo("secret-A-only");
        assertThat(secretEncryptionService.decrypt(credB.getOauthClientSecretCiphertext()))
                .isEqualTo("secret-B-only");
        assertThat(secretEncryptionService.decrypt(credA.getSecretCiphertext())).isEqualTo("access-client-A");
        assertThat(secretEncryptionService.decrypt(credB.getSecretCiphertext())).isEqualTo("access-client-B");

        ArgumentCaptor<OpenProjectOAuthClientCredentials> clients =
                ArgumentCaptor.forClass(OpenProjectOAuthClientCredentials.class);
        verify(oauthClient, atLeastOnce()).exchangeAuthorizationCode(anyString(), anyString(), anyString(), clients.capture());
        assertThat(clients.getAllValues())
                .extracting(OpenProjectOAuthClientCredentials::clientId)
                .contains("client-A", "client-B");
        assertThat(clients.getAllValues())
                .noneMatch(c -> "client-A".equals(c.clientId()) && "secret-B-only".equals(c.clientSecret()));
        assertThat(clients.getAllValues())
                .noneMatch(c -> "client-B".equals(c.clientId()) && "secret-A-only".equals(c.clientSecret()));
    }

    @Test
    @DisplayName("oauth callback denies ineligible OpenProject identity")
    void oauthCallbackIneligible() {
        when(eligibilityService.evaluate(any())).thenReturn(
                new OpenProjectEligibilityService.EligibilityResult(
                        false, 7L, "bob", "bob@op.test", false, List.of("Member"), "lacks required permissions"
                )
        );
        when(oauthClient.exchangeAuthorizationCode(anyString(), anyString(), anyString(), any()))
                .thenReturn(new OpenProjectOAuthTokenResponse(
                        "access-token-2", "refresh-token-2", "Bearer", "api_v3", Instant.now().plusSeconds(7200)
                ));

        String token = login("admin", "Admin123!");
        String baseUrl = "https://op-oauth-deny-" + UUID.randomUUID() + ".test";
        String state = startOAuth(token, baseUrl, "client-x", "secret-x");
        ResponseEntity<Void> callback = getCallbackNoFollow(
                "/api/v1/workspaces/oauth/callback?code=auth-code-2&state=" + state
        );
        assertThat(callback.getStatusCode()).isEqualTo(HttpStatus.FOUND);
        assertThat(callback.getHeaders().getLocation().toString()).contains("oauth=error");
        assertThat(workspaceRepository.findByBaseUrlIgnoreCase(baseUrl)).isEmpty();
    }

    @Test
    @DisplayName("second eligible PA user OAuth to same OP URL is denied (M15), no duplicate workspace")
    void secondEligibleUserDeniedWithoutSystemError() {
        when(eligibilityService.evaluate(any())).thenReturn(
                new OpenProjectEligibilityService.EligibilityResult(
                        true, 55L, "op-user", "op@example.test", true, List.of("Project admin"), "eligible"
                )
        );
        when(oauthClient.exchangeAuthorizationCode(anyString(), anyString(), anyString(), any()))
                .thenAnswer(inv -> new OpenProjectOAuthTokenResponse(
                        "access-" + inv.getArgument(1),
                        "refresh-" + inv.getArgument(1),
                        "Bearer",
                        "api_v3",
                        Instant.now().plusSeconds(7200)
                ));

        String baseUrl = "https://op-oauth-shared-" + UUID.randomUUID() + ".test";
        long workspacesBefore = workspaceRepository.count();

        String adminToken = login("admin", "Admin123!");
        oauthConnect(adminToken, baseUrl, "Shared OP", "client-shared", "secret-shared");
        assertThat(workspaceRepository.count()).isEqualTo(workspacesBefore + 1);

        String email = "second_" + System.nanoTime() + "@example.test";
        String username = "sec_" + System.nanoTime();
        String secondToken = AuthTestSupport.registerConfirmAndLogin(
                restTemplate, mailLinkCaptor, email, "Welcome123!", username
        );

        String state2 = startOAuth(secondToken, baseUrl, "client-shared", "secret-shared");
        ResponseEntity<Void> callback2 = getCallbackNoFollow(
                "/api/v1/workspaces/oauth/callback?code=code-second&state=" + state2
        );
        assertThat(callback2.getStatusCode()).isEqualTo(HttpStatus.FOUND);
        String decoded = URLDecoder.decode(callback2.getHeaders().getLocation().toString(), StandardCharsets.UTF_8)
                .toLowerCase();
        assertThat(decoded).contains("oauth=error");
        assertThat(decoded).doesNotContain("system_001");
        assertThat(decoded).contains("already connected");
        assertThat(workspaceRepository.count()).isEqualTo(workspacesBefore + 1);

        ResponseEntity<Map> secondList = restTemplate.exchange(
                "/api/v1/workspaces",
                HttpMethod.GET,
                new HttpEntity<>(bearer(secondToken)),
                Map.class
        );
        @SuppressWarnings("unchecked")
        List<?> secondWorkspaces = (List<?>) secondList.getBody().get("data");
        assertThat(secondWorkspaces).isEmpty();
    }

    private void oauthConnect(String token, String baseUrl, String name, String clientId, String clientSecret) {
        String state = startOAuth(token, baseUrl, name, clientId, clientSecret);
        ResponseEntity<Void> callback = getCallbackNoFollow(
                "/api/v1/workspaces/oauth/callback?code=code-" + clientId + "&state=" + state
        );
        assertThat(callback.getStatusCode()).isEqualTo(HttpStatus.FOUND);
        assertThat(callback.getHeaders().getLocation().toString()).contains("oauth=success");
    }

    private String startOAuth(String token, String baseUrl, String clientId, String clientSecret) {
        return startOAuth(token, baseUrl, null, clientId, clientSecret);
    }

    private String startOAuth(String token, String baseUrl, String name, String clientId, String clientSecret) {
        Map<String, Object> body = new java.util.HashMap<>();
        body.put("baseUrl", baseUrl);
        body.put("clientId", clientId);
        body.put("clientSecret", clientSecret);
        if (name != null) {
            body.put("name", name);
        }
        ResponseEntity<Map> start = restTemplate.exchange(
                "/api/v1/workspaces/oauth/start",
                HttpMethod.POST,
                new HttpEntity<>(body, bearer(token)),
                Map.class
        );
        assertThat(start.getStatusCode()).isEqualTo(HttpStatus.OK);
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) start.getBody().get("data");
        return (String) data.get("state");
    }

    private ResponseEntity<Void> getCallbackNoFollow(String path) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory() {
            @Override
            protected void prepareConnection(HttpURLConnection connection, String httpMethod) throws IOException {
                super.prepareConnection(connection, httpMethod);
                connection.setInstanceFollowRedirects(false);
            }
        };
        RestTemplate noFollow = new RestTemplate(factory);
        return noFollow.exchange(
                "http://localhost:" + port + path,
                HttpMethod.GET,
                null,
                Void.class
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
