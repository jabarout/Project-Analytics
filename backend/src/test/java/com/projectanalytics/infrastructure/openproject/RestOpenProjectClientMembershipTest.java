package com.projectanalytics.infrastructure.openproject;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Parses real OpenProject membership HAL shapes (projectana sample).
 */
class RestOpenProjectClientMembershipTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    @DisplayName("detects Project admin role and principal from real OP membership JSON")
    void parsesProjectAdminMembership() throws Exception {
        String json = """
                {
                  "_type": "Membership",
                  "id": 2,
                  "_links": {
                    "self": { "href": "/api/v3/memberships/2", "title": "ali DAGHER" },
                    "project": { "href": "/api/v3/projects/2", "title": "Scrum project" },
                    "principal": { "href": "/api/v3/users/4", "title": "ali DAGHER" },
                    "roles": [{ "href": "/api/v3/roles/5", "title": "Project admin" }]
                  }
                }
                """;
        JsonNode node = mapper.readTree(json);

        assertThat(RestOpenProjectClient.isProjectAdminMembership(node)).isTrue();
        assertThat(RestOpenProjectClient.extractPrincipalName(node)).isEqualTo("ali DAGHER");
        assertThat(RestOpenProjectClient.extractIdFromHref("/api/v3/projects/2")).isEqualTo(2L);
        assertThat(RestOpenProjectClient.extractIdFromHref("https://host/api/v3/projects/2")).isEqualTo(2L);
        assertThat(RestOpenProjectClient.isAdminRoleTitle("Project admin")).isTrue();
    }

    @Test
    @DisplayName("handles single role object (non-array HAL)")
    void singleRoleObject() throws Exception {
        String json = """
                {
                  "_links": {
                    "principal": { "title": "Bob" },
                    "project": { "href": "/api/v3/projects/9" },
                    "roles": { "href": "/api/v3/roles/5", "title": "Project admin" }
                  }
                }
                """;
        JsonNode node = mapper.readTree(json);
        assertThat(RestOpenProjectClient.isProjectAdminMembership(node)).isTrue();
        assertThat(RestOpenProjectClient.extractPrincipalName(node)).isEqualTo("Bob");
    }
}
