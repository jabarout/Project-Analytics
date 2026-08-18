package com.projectanalytics.project.api;

import com.projectanalytics.analytics.api.dto.ProjectDashboardResponse;
import com.projectanalytics.analytics.api.dto.ProjectWorkPackageAnalyticsResponse;
import com.projectanalytics.analytics.application.AnalyticsQueryService;
import com.projectanalytics.authentication.security.AuthenticatedUser;
import com.projectanalytics.common.api.ApiResponse;
import com.projectanalytics.common.constants.ApplicationConstants;
import com.projectanalytics.synchronization.application.WorkspaceAccessService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Project presentation APIs. Business scores come from the analytics module only.
 */
@RestController
@RequestMapping(ApplicationConstants.API_V1_BASE_PATH + "/projects")
@Tag(name = "Projects", description = "Project dashboards from local analytics")
@SecurityRequirement(name = "bearerAuth")
public class ProjectController {

    private final AnalyticsQueryService analyticsQueryService;
    private final WorkspaceAccessService workspaceAccessService;

    public ProjectController(
            AnalyticsQueryService analyticsQueryService,
            WorkspaceAccessService workspaceAccessService
    ) {
        this.analyticsQueryService = analyticsQueryService;
        this.workspaceAccessService = workspaceAccessService;
    }

    @GetMapping("/{id}/dashboard")
    @Operation(summary = "Project dashboard", description = "Uses the shared analytics engine DTOs.")
    public ApiResponse<ProjectDashboardResponse> dashboard(
            @PathVariable UUID id,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        workspaceAccessService.requireAnalyticsAccessForProject(id, user.getId());
        return ApiResponse.of(analyticsQueryService.getProjectDashboard(id));
    }

    @GetMapping("/{id}/work-package-analytics")
    @Operation(
            summary = "Project work-package analytics",
            description = "Local operational WP summary, overdue list, assignee bottlenecks, status distribution."
    )
    public ApiResponse<ProjectWorkPackageAnalyticsResponse> workPackageAnalytics(
            @PathVariable UUID id,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        workspaceAccessService.requireAnalyticsAccessForProject(id, user.getId());
        return ApiResponse.of(analyticsQueryService.getProjectWorkPackageAnalytics(id));
    }
}
