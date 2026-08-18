package com.projectanalytics.infrastructure.openproject;

import com.fasterxml.jackson.databind.JsonNode;
import com.projectanalytics.common.exception.BusinessException;
import com.projectanalytics.common.exception.ErrorCode;
import com.projectanalytics.infrastructure.openproject.dto.OpenProjectProjectDto;
import com.projectanalytics.infrastructure.openproject.dto.OpenProjectWorkPackageDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.ClientHttpRequestFactories;
import org.springframework.boot.web.client.ClientHttpRequestFactorySettings;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * OpenProject REST API v3 client.
 *
 * <p>HTTP authentication is applied centrally from {@link OpenProjectCredentials}
 * (API key Basic auth today; Bearer reserved for future OAuth 2.0).
 * No analytics are performed here.
 */
@Component
public class RestOpenProjectClient implements OpenProjectClient {

    private static final Logger log = LoggerFactory.getLogger(RestOpenProjectClient.class);
    private static final int PAGE_SIZE = 100;

    private final RestClient.Builder restClientBuilder;

    public RestOpenProjectClient(RestClient.Builder restClientBuilder) {
        this.restClientBuilder = restClientBuilder;
    }

    @Override
    public List<OpenProjectProjectDto> fetchProjects(
            OpenProjectConnectionProperties connection,
            Instant modifiedSince
    ) {
        List<OpenProjectProjectDto> projects = new ArrayList<>();
        int offset = 1;
        while (true) {
            String path = "/api/v3/projects?pageSize=" + PAGE_SIZE + "&offset=" + offset;
            if (modifiedSince != null) {
                path += "&filters=" + encodeFilters(updatedAfterFilter(modifiedSince));
            }
            JsonNode root = getJson(connection, path);
            JsonNode elements = root.path("_embedded").path("elements");
            if (!elements.isArray() || elements.isEmpty()) {
                break;
            }
            for (JsonNode element : elements) {
                projects.add(mapProject(element, null));
            }
            if (elements.size() < PAGE_SIZE) {
                break;
            }
            // OpenProject API v3 "offset" is the 1-based page number, not a row offset.
            offset += 1;
        }
        // Admin enrichment is applied in OperationalDataImportService for all local projects
        // (including ones skipped by incremental project filters). Still attach when present.
        Map<Long, List<String>> adminsByProject = fetchProjectAdminNamesByProjectId(connection);
        if (adminsByProject.isEmpty()) {
            log.info("OpenProject memberships returned no project admins for {}", connection.baseUrl());
            return projects;
        }
        log.info(
                "OpenProject project admins resolved for {} project(s) on {}",
                adminsByProject.size(),
                connection.baseUrl()
        );
        List<OpenProjectProjectDto> enriched = new ArrayList<>(projects.size());
        for (OpenProjectProjectDto project : projects) {
            List<String> admins = adminsByProject.get(project.id());
            String adminName = (admins == null || admins.isEmpty()) ? null : String.join(", ", admins);
            enriched.add(new OpenProjectProjectDto(
                    project.id(),
                    project.name(),
                    project.description(),
                    project.status(),
                    project.startDate(),
                    project.endDate(),
                    project.updatedAt(),
                    adminName
            ));
        }
        return enriched;
    }

    @Override
    public Map<Long, List<String>> fetchProjectAdminNamesByProjectId(OpenProjectConnectionProperties connection) {
        Map<Long, Set<String>> names = new HashMap<>();
        int offset = 1;
        int membershipCount = 0;
        int adminMembershipCount = 0;
        while (true) {
            String path = "/api/v3/memberships?pageSize=" + PAGE_SIZE + "&offset=" + offset;
            JsonNode root = getJson(connection, path);
            JsonNode elements = root.path("_embedded").path("elements");
            if (!elements.isArray() || elements.isEmpty()) {
                break;
            }
            for (JsonNode element : elements) {
                membershipCount++;
                if (!isProjectAdminMembership(element)) {
                    continue;
                }
                adminMembershipCount++;
                Long projectId = extractIdFromHref(element.path("_links").path("project").path("href").asText(null));
                String principal = extractPrincipalName(element);
                if (projectId == null || principal == null || principal.isBlank()) {
                    log.debug(
                            "Skipped admin membership id={} projectId={} principal={}",
                            element.path("id").asText(null),
                            projectId,
                            principal
                    );
                    continue;
                }
                names.computeIfAbsent(projectId, ignored -> new LinkedHashSet<>()).add(principal.trim());
            }
            if (elements.size() < PAGE_SIZE) {
                break;
            }
            offset += 1;
        }
        log.info(
                "OpenProject memberships scanned={} adminRoleMatches={} projectsWithAdmin={}",
                membershipCount,
                adminMembershipCount,
                names.size()
        );
        Map<Long, List<String>> result = new HashMap<>();
        names.forEach((id, set) -> result.put(id, List.copyOf(set)));
        return result;
    }

    @Override
    public List<OpenProjectWorkPackageDto> fetchWorkPackages(
            OpenProjectConnectionProperties connection,
            long openProjectProjectId,
            Instant modifiedSince
    ) {
        List<OpenProjectWorkPackageDto> workPackages = new ArrayList<>();
        int offset = 1;
        while (true) {
            String filters = projectFilter(openProjectProjectId);
            if (modifiedSince != null) {
                filters = "[" + stripOuterBrackets(filters) + "," + stripOuterBrackets(updatedAfterFilter(modifiedSince)) + "]";
            }
            String path = "/api/v3/work_packages?pageSize=" + PAGE_SIZE
                    + "&offset=" + offset
                    + "&filters=" + encodeFilters(filters);
            JsonNode root = getJson(connection, path);
            JsonNode elements = root.path("_embedded").path("elements");
            if (!elements.isArray() || elements.isEmpty()) {
                break;
            }
            for (JsonNode element : elements) {
                workPackages.add(mapWorkPackage(element, openProjectProjectId));
            }
            if (elements.size() < PAGE_SIZE) {
                break;
            }
            // OpenProject API v3 "offset" is the 1-based page number, not a row offset.
            offset += 1;
        }
        return workPackages;
    }

    @Override
    public String fetchServerVersion(OpenProjectConnectionProperties connection) {
        JsonNode root = getJson(connection, "/api/v3");
        JsonNode coreVersion = root.path("coreVersion");
        if (!coreVersion.isMissingNode() && !coreVersion.isNull()) {
            return coreVersion.asText();
        }
        JsonNode instanceName = root.path("instanceName");
        return instanceName.isMissingNode() ? null : instanceName.asText();
    }

    private JsonNode getJson(OpenProjectConnectionProperties connection, String path) {
        try {
            RestClient client = buildClient(connection);
            // Use URI.create so pre-encoded query values (e.g. filters) are not double-encoded by
            // RestClient's default UriBuilder (double-encoding yields OpenProject HTTP 400 InvalidQuery).
            return client.get()
                    .uri(URI.create(path))
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(JsonNode.class);
        } catch (ResourceAccessException exception) {
            log.warn("OpenProject timeout/unreachable baseUrl={} path={}", connection.baseUrl(), path);
            throw new BusinessException(ErrorCode.SYNC_002, ErrorCode.SYNC_002.getDefaultMessage(), exception);
        } catch (RestClientResponseException exception) {
            log.warn(
                    "OpenProject HTTP error status={} baseUrl={} path={} body={}",
                    exception.getStatusCode().value(),
                    connection.baseUrl(),
                    path,
                    truncate(exception.getResponseBodyAsString(), 300)
            );
            throw new BusinessException(ErrorCode.SYNC_004, ErrorCode.SYNC_004.getDefaultMessage(), exception);
        } catch (RestClientException exception) {
            log.warn("OpenProject client error baseUrl={} path={}", connection.baseUrl(), path);
            throw new BusinessException(ErrorCode.SYNC_004, ErrorCode.SYNC_004.getDefaultMessage(), exception);
        }
    }

    private static String truncate(String value, int max) {
        if (value == null) {
            return "";
        }
        String trimmed = value.replaceAll("\\s+", " ").trim();
        return trimmed.length() <= max ? trimmed : trimmed.substring(0, max) + "…";
    }

    private RestClient buildClient(OpenProjectConnectionProperties connection) {
        String baseUrl = normalizeBaseUrl(connection.baseUrl());
        Duration timeout = Duration.ofSeconds(Math.max(1, connection.timeoutSeconds()));
        ClientHttpRequestFactory requestFactory = ClientHttpRequestFactories.get(
                ClientHttpRequestFactorySettings.DEFAULTS
                        .withConnectTimeout(timeout)
                        .withReadTimeout(timeout)
        );
        return restClientBuilder
                .clone()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .defaultHeader("Authorization", buildAuthorizationHeader(connection.credentials()))
                .build();
    }

    /**
     * Single place that turns resolved credentials into an HTTP Authorization header.
     */
    static String buildAuthorizationHeader(OpenProjectCredentials credentials) {
        if (credentials == null || credentials.scheme() == null) {
            throw new BusinessException(ErrorCode.SYNC_005, "OpenProject credentials are not configured.");
        }
        return switch (credentials.scheme()) {
            case API_KEY -> {
                if (credentials.apiKey() == null || credentials.apiKey().isBlank()) {
                    throw new BusinessException(ErrorCode.SYNC_005, "OpenProject API key is not configured.");
                }
                String basic = Base64.getEncoder().encodeToString(
                        ("apikey:" + credentials.apiKey()).getBytes(StandardCharsets.UTF_8)
                );
                yield "Basic " + basic;
            }
            case BEARER_TOKEN -> {
                // Reserved for a future OAuth 2.0 access token. Not used by the default resolver.
                if (credentials.accessToken() == null || credentials.accessToken().isBlank()) {
                    throw new BusinessException(ErrorCode.SYNC_005, "OpenProject access token is not configured.");
                }
                yield "Bearer " + credentials.accessToken();
            }
        };
    }

    private OpenProjectProjectDto mapProject(JsonNode node, String adminName) {
        try {
            long id = node.path("id").asLong();
            String name = textOrDefault(node, "name", "Unnamed project");
            String description = extractDescription(node.path("description"));
            // OP has two concepts: `active` (not archived) and status title (On track / At risk / …).
            // Archived always wins; otherwise prefer the human status title for display.
            boolean active = !node.path("active").isBoolean() || node.path("active").asBoolean(true);
            String status;
            if (!active) {
                status = "ARCHIVED";
            } else {
                String statusTitle = node.path("_links").path("status").path("title").asText(null);
                if (statusTitle != null && !statusTitle.isBlank()) {
                    status = statusTitle.trim();
                } else {
                    status = "ACTIVE";
                }
            }
            return new OpenProjectProjectDto(
                    id,
                    name,
                    description,
                    status,
                    parseDate(node.path("startDate").asText(null)),
                    parseDate(node.path("endDate").asText(node.path("dueDate").asText(null))),
                    parseInstant(node.path("updatedAt").asText(null)),
                    adminName
            );
        } catch (RuntimeException exception) {
            throw new BusinessException(ErrorCode.SYNC_006, ErrorCode.SYNC_006.getDefaultMessage(), exception);
        }
    }

    /**
     * True when membership roles include Project admin (or common manager-equivalent titles).
     * HAL may return {@code roles} as an array or a single object.
     */
    static boolean isProjectAdminMembership(JsonNode membership) {
        for (String title : extractRoleTitles(membership)) {
            if (isAdminRoleTitle(title)) {
                return true;
            }
        }
        return false;
    }

    static List<String> extractRoleTitles(JsonNode membership) {
        List<String> titles = new ArrayList<>();
        collectRoleTitles(membership.path("_links").path("roles"), titles);
        collectRoleTitles(membership.path("_embedded").path("roles"), titles);
        return titles;
    }

    private static void collectRoleTitles(JsonNode rolesNode, List<String> titles) {
        if (rolesNode == null || rolesNode.isMissingNode() || rolesNode.isNull()) {
            return;
        }
        if (rolesNode.isArray()) {
            for (JsonNode role : rolesNode) {
                String title = role.path("title").asText(null);
                if (title == null || title.isBlank()) {
                    title = role.path("name").asText(null);
                }
                if (title != null && !title.isBlank()) {
                    titles.add(title);
                }
            }
            return;
        }
        // Single role object (HAL sometimes omits array for one element)
        String title = rolesNode.path("title").asText(null);
        if (title == null || title.isBlank()) {
            title = rolesNode.path("name").asText(null);
        }
        if (title != null && !title.isBlank()) {
            titles.add(title);
        }
    }

    static String extractPrincipalName(JsonNode membership) {
        String principal = membership.path("_links").path("principal").path("title").asText(null);
        if (principal != null && !principal.isBlank()) {
            return principal.trim();
        }
        principal = membership.path("_links").path("self").path("title").asText(null);
        if (principal != null && !principal.isBlank()) {
            return principal.trim();
        }
        principal = membership.path("_embedded").path("principal").path("name").asText(null);
        if (principal != null && !principal.isBlank()) {
            return principal.trim();
        }
        return null;
    }

    static boolean isAdminRoleTitle(String title) {
        if (title == null || title.isBlank()) {
            return false;
        }
        String n = title.trim().toLowerCase(Locale.ROOT);
        // Match common OpenProject role titles (EN + common variants).
        return n.equals("project admin")
                || n.contains("project admin")
                || n.equals("admin")
                || n.equals("administrator")
                || n.contains("administrator")
                || n.equals("manager")
                || n.equals("project manager")
                || n.contains("project manager");
    }

    static Long extractIdFromHref(String href) {
        if (href == null || href.isBlank()) {
            return null;
        }
        // Accept absolute or relative hrefs: /api/v3/projects/2 or https://host/api/v3/projects/2
        int slash = href.lastIndexOf('/');
        if (slash < 0 || slash == href.length() - 1) {
            return null;
        }
        String tail = href.substring(slash + 1);
        // Drop query/fragment if present
        int q = tail.indexOf('?');
        if (q >= 0) {
            tail = tail.substring(0, q);
        }
        int hash = tail.indexOf('#');
        if (hash >= 0) {
            tail = tail.substring(0, hash);
        }
        try {
            return Long.parseLong(tail);
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private OpenProjectWorkPackageDto mapWorkPackage(JsonNode node, long fallbackProjectId) {
        try {
            long id = node.path("id").asLong();
            long projectId = extractProjectId(node, fallbackProjectId);
            String subject = textOrDefault(node, "subject", "Untitled work package");
            return new OpenProjectWorkPackageDto(
                    id,
                    projectId,
                    subject,
                    linkTitle(node, "type"),
                    linkTitle(node, "status"),
                    linkTitle(node, "priority"),
                    linkTitle(node, "assignee"),
                    parseIsoDurationHours(node.path("estimatedTime").asText(null)),
                    parseIsoDurationHours(node.path("spentTime").asText(null)),
                    parseDate(node.path("dueDate").asText(null)),
                    parseInstant(node.path("updatedAt").asText(null))
            );
        } catch (BusinessException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            throw new BusinessException(ErrorCode.SYNC_006, ErrorCode.SYNC_006.getDefaultMessage(), exception);
        }
    }

    private long extractProjectId(JsonNode node, long fallbackProjectId) {
        JsonNode href = node.path("_links").path("project").path("href");
        if (href.isMissingNode() || href.isNull()) {
            return fallbackProjectId;
        }
        String value = href.asText();
        int slash = value.lastIndexOf('/');
        if (slash < 0 || slash == value.length() - 1) {
            return fallbackProjectId;
        }
        try {
            return Long.parseLong(value.substring(slash + 1));
        } catch (NumberFormatException exception) {
            return fallbackProjectId;
        }
    }

    private static String linkTitle(JsonNode node, String rel) {
        JsonNode title = node.path("_links").path(rel).path("title");
        return title.isMissingNode() || title.isNull() ? null : title.asText();
    }

    private static String extractDescription(JsonNode descriptionNode) {
        if (descriptionNode == null || descriptionNode.isMissingNode() || descriptionNode.isNull()) {
            return null;
        }
        if (descriptionNode.isTextual()) {
            return descriptionNode.asText();
        }
        JsonNode raw = descriptionNode.path("raw");
        if (!raw.isMissingNode() && !raw.isNull()) {
            return raw.asText();
        }
        return descriptionNode.toString();
    }

    private static String textOrDefault(JsonNode node, String field, String defaultValue) {
        JsonNode value = node.path(field);
        if (value.isMissingNode() || value.isNull() || value.asText().isBlank()) {
            return defaultValue;
        }
        return value.asText();
    }

    private static LocalDate parseDate(String value) {
        if (value == null || value.isBlank() || "null".equalsIgnoreCase(value)) {
            return null;
        }
        try {
            return LocalDate.parse(value.substring(0, Math.min(10, value.length())));
        } catch (DateTimeParseException exception) {
            return null;
        }
    }

    private static Instant parseInstant(String value) {
        if (value == null || value.isBlank() || "null".equalsIgnoreCase(value)) {
            return null;
        }
        try {
            return Instant.parse(value);
        } catch (DateTimeParseException exception) {
            return null;
        }
    }

    /**
     * Parses ISO-8601 durations such as {@code PT8H30M} into decimal hours.
     */
    static BigDecimal parseIsoDurationHours(String value) {
        if (value == null || value.isBlank() || "null".equalsIgnoreCase(value)) {
            return null;
        }
        try {
            Duration duration = Duration.parse(value);
            return BigDecimal.valueOf(duration.toMinutes())
                    .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
        } catch (DateTimeParseException exception) {
            return null;
        }
    }

    private static String updatedAfterFilter(Instant modifiedSince) {
        // OpenProject JSON filters: [{"updatedAt":{"operator":"<>d","values":["from",""]}}]
        return "[{\"updatedAt\":{\"operator\":\"<>d\",\"values\":[\"" + modifiedSince + "\",\"\"]}}]";
    }

    private static String projectFilter(long projectId) {
        return "[{\"project\":{\"operator\":\"=\",\"values\":[\"" + projectId + "\"]}}]";
    }

    private static String stripOuterBrackets(String jsonArray) {
        String trimmed = jsonArray.trim();
        if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
            return trimmed.substring(1, trimmed.length() - 1);
        }
        return trimmed;
    }

    private static String encodeFilters(String filtersJson) {
        return URLEncoder.encode(filtersJson, StandardCharsets.UTF_8);
    }

    private static String normalizeBaseUrl(String baseUrl) {
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new BusinessException(ErrorCode.SYNC_005, "OpenProject base URL is required.");
        }
        String normalized = baseUrl.trim();
        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }
}
