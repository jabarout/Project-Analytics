package com.projectanalytics.portfolio.api.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;
import java.util.UUID;

/**
 * Bulk add of project memberships to a portfolio (organizational only).
 */
public record BulkAssignProjectsRequest(
        @NotEmpty List<UUID> projectIds
) {
    public BulkAssignProjectsRequest {
        projectIds = projectIds == null ? List.of() : List.copyOf(projectIds);
    }
}
