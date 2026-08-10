package com.projectanalytics.portfolio.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

/**
 * Create an analytical portfolio. Optional projectIds are added as memberships
 * (same workspace only); they do not change project ownership.
 */
public record CreatePortfolioRequest(
        @NotNull UUID workspaceId,
        @NotBlank @Size(max = 200) String name,
        @Size(max = 4000) String description,
        /**
         * Optional initial members. Same-workspace projects only; membership is organizational.
         */
        List<UUID> projectIds
) {
    public CreatePortfolioRequest {
        projectIds = projectIds == null ? List.of() : List.copyOf(projectIds);
    }

    /** Convenience for callers that create a portfolio without initial members. */
    public CreatePortfolioRequest(UUID workspaceId, String name, String description) {
        this(workspaceId, name, description, List.of());
    }
}
