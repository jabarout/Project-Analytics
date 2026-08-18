package com.projectanalytics.infrastructure.openproject;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.projectanalytics.common.exception.BusinessException;
import com.projectanalytics.common.exception.ErrorCode;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Talks to OpenProject OAuth endpoints ({@code /oauth/authorize}, {@code /oauth/token}).
 * Client id/secret are supplied per call (per-workspace or optional global default).
 */
@Component
public class OpenProjectOAuthClient {

    private final ObjectMapper objectMapper;
    private final OpenProjectOAuthProperties oauthProperties;
    private final OpenProjectProperties openProjectProperties;
    private final HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();

    public OpenProjectOAuthClient(
            ObjectMapper objectMapper,
            OpenProjectOAuthProperties oauthProperties,
            OpenProjectProperties openProjectProperties
    ) {
        this.objectMapper = objectMapper;
        this.oauthProperties = oauthProperties;
        this.openProjectProperties = openProjectProperties;
    }

    public OpenProjectOAuthTokenResponse exchangeAuthorizationCode(
            String baseUrl,
            String code,
            String codeVerifier,
            OpenProjectOAuthClientCredentials client
    ) {
        Map<String, String> form = new LinkedHashMap<>();
        form.put("grant_type", "authorization_code");
        form.put("client_id", client.clientId());
        form.put("client_secret", client.clientSecret());
        form.put("code", code);
        form.put("redirect_uri", oauthProperties.getRedirectUri());
        form.put("code_verifier", codeVerifier);
        return postToken(normalize(baseUrl) + "/oauth/token", form);
    }

    public OpenProjectOAuthTokenResponse refreshAccessToken(
            String baseUrl,
            String refreshToken,
            OpenProjectOAuthClientCredentials client
    ) {
        Map<String, String> form = new LinkedHashMap<>();
        form.put("grant_type", "refresh_token");
        form.put("client_id", client.clientId());
        form.put("client_secret", client.clientSecret());
        form.put("refresh_token", refreshToken);
        return postToken(normalize(baseUrl) + "/oauth/token", form);
    }

    private OpenProjectOAuthTokenResponse postToken(String tokenUrl, Map<String, String> form) {
        try {
            String body = form.entrySet().stream()
                    .map(e -> urlEncode(e.getKey()) + "=" + urlEncode(e.getValue() == null ? "" : e.getValue()))
                    .collect(Collectors.joining("&"));
            HttpRequest request = HttpRequest.newBuilder(URI.create(tokenUrl))
                    .timeout(Duration.ofSeconds(Math.max(5, openProjectProperties.getTimeoutSeconds())))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new BusinessException(
                        ErrorCode.SYNC_005,
                        "OpenProject OAuth token request failed (HTTP " + response.statusCode() + ")."
                );
            }
            JsonNode json = objectMapper.readTree(response.body());
            String accessToken = text(json, "access_token");
            if (accessToken == null || accessToken.isBlank()) {
                throw new BusinessException(ErrorCode.SYNC_005, "OpenProject OAuth response missing access_token.");
            }
            long expiresIn = json.path("expires_in").asLong(7200);
            Instant expiresAt = Instant.now().plusSeconds(Math.max(60, expiresIn));
            return new OpenProjectOAuthTokenResponse(
                    accessToken,
                    text(json, "refresh_token"),
                    text(json, "token_type"),
                    text(json, "scope"),
                    expiresAt
            );
        } catch (BusinessException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new BusinessException(
                    ErrorCode.SYNC_005,
                    "Unable to complete OpenProject OAuth token exchange: " + exception.getMessage(),
                    exception
            );
        }
    }

    private static String text(JsonNode node, String field) {
        String value = node.path(field).asText(null);
        return value == null || value.isBlank() ? null : value.trim();
    }

    private static String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private static String normalize(String baseUrl) {
        String normalized = baseUrl.trim();
        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }
}
