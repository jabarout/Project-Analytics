package com.projectanalytics.synchronization.application;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.projectanalytics.common.exception.BusinessException;
import com.projectanalytics.common.exception.ErrorCode;
import com.projectanalytics.infrastructure.openproject.OpenProjectConnectionProperties;
import com.projectanalytics.infrastructure.openproject.OpenProjectCredentials;
import com.projectanalytics.infrastructure.openproject.RestOpenProjectClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Determines whether an OpenProject identity may establish a Project Analytics workspace (M14).
 * OAuth/API-key success alone is not enough — role/admin check is required.
 */
@Service
public class OpenProjectEligibilityService {

    private final ObjectMapper objectMapper;
    private final Set<String> allowedRoleTitles;
    private final HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();

    public OpenProjectEligibilityService(
            ObjectMapper objectMapper,
            @Value("${projectanalytics.security.openproject.eligible-role-titles:Project admin}") String eligibleRoleTitles
    ) {
        this.objectMapper = objectMapper;
        this.allowedRoleTitles = Arrays.stream(eligibleRoleTitles.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(s -> s.toLowerCase(Locale.ROOT))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public EligibilityResult evaluate(OpenProjectConnectionProperties connection) {
        try {
            String auth = RestOpenProjectClient.buildAuthorizationHeader(connection.credentials());
            JsonNode me = getJson(connection.baseUrl() + "/api/v3/users/me", auth, connection.timeoutSeconds());
            long userId = me.path("id").asLong(0);
            String login = text(me, "login");
            String email = text(me, "email");
            boolean admin = me.path("admin").asBoolean(false);

            List<String> roleTitles = new ArrayList<>();
            if (userId > 0) {
                String filter = URLEncoder.encode(
                        "[{\"principal\":{\"operator\":\"=\",\"values\":[\"" + userId + "\"]}}]",
                        StandardCharsets.UTF_8
                );
                JsonNode memberships = getJson(
                        connection.baseUrl() + "/api/v3/memberships?pageSize=200&filters=" + filter,
                        auth,
                        connection.timeoutSeconds()
                );
                for (JsonNode element : memberships.path("_embedded").path("elements")) {
                    JsonNode roles = element.path("_links").path("roles");
                    if (roles.isArray()) {
                        for (JsonNode role : roles) {
                            String title = role.path("title").asText(null);
                            if (title != null && !title.isBlank()) {
                                roleTitles.add(title.trim());
                            }
                        }
                    } else if (roles.isObject()) {
                        String title = roles.path("title").asText(null);
                        if (title != null && !title.isBlank()) {
                            roleTitles.add(title.trim());
                        }
                    }
                }
            }

            boolean roleEligible = roleTitles.stream()
                    .map(t -> t.toLowerCase(Locale.ROOT))
                    .anyMatch(allowedRoleTitles::contains);

            boolean eligible = admin || roleEligible;
            String reason;
            if (admin) {
                reason = "OpenProject global administrator";
            } else if (roleEligible) {
                reason = "OpenProject role matches allow-list: " + String.join(", ", distinct(roleTitles));
            } else {
                reason = "OpenProject account lacks required permissions (need global admin or "
                        + String.join(" / ", allowedRoleTitles) + "). Roles found: "
                        + (roleTitles.isEmpty() ? "(none)" : String.join(", ", distinct(roleTitles)));
            }

            return new EligibilityResult(
                    eligible,
                    userId > 0 ? userId : null,
                    login,
                    email,
                    admin,
                    List.copyOf(distinct(roleTitles)),
                    reason
            );
        } catch (BusinessException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new BusinessException(
                    ErrorCode.SYNC_005,
                    "Unable to verify OpenProject eligibility: " + exception.getMessage(),
                    exception
            );
        }
    }

    private JsonNode getJson(String url, String authorization, int timeoutSeconds) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(Math.max(5, timeoutSeconds)))
                .header("Authorization", authorization)
                .header("Accept", "application/hal+json")
                .GET()
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 401 || response.statusCode() == 403) {
            throw new BusinessException(ErrorCode.SYNC_005, "OpenProject credentials were rejected (HTTP " + response.statusCode() + ").");
        }
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new BusinessException(ErrorCode.SYNC_004, "OpenProject eligibility probe failed (HTTP " + response.statusCode() + ").");
        }
        return objectMapper.readTree(response.body());
    }

    private static String text(JsonNode node, String field) {
        String value = node.path(field).asText(null);
        return value == null || value.isBlank() ? null : value.trim();
    }

    private static List<String> distinct(List<String> values) {
        return values.stream().distinct().toList();
    }

    public record EligibilityResult(
            boolean eligible,
            Long openProjectUserId,
            String openProjectLogin,
            String openProjectEmail,
            boolean openProjectAdmin,
            List<String> roleTitles,
            String reason
    ) {
    }
}
