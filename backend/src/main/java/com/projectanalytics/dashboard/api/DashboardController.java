package com.projectanalytics.dashboard.api;

import com.projectanalytics.analytics.api.dto.ScopeDashboardResponse;
import com.projectanalytics.analytics.application.AnalyticsQueryService;
import com.projectanalytics.authentication.security.AuthenticatedUser;
import com.projectanalytics.common.api.ApiResponse;
import com.projectanalytics.common.constants.ApplicationConstants;
import com.projectanalytics.dashboard.api.dto.ExecutiveDashboardResponse;
import com.projectanalytics.dashboard.application.ExecutiveDashboardService;
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
 * Presentation dashboard endpoints. Composes analytics DTOs only — no scoring formulas.
 */
@RestController
@RequestMapping(ApplicationConstants.API_V1_BASE_PATH + "/dashboards")
@Tag(name = "Dashboards", description = "Executive and scoped dashboards (visualization layer)")
@SecurityRequirement(name = "bearerAuth")
public class DashboardController {

    private final ExecutiveDashboardService executiveDashboardService;
    private final AnalyticsQueryService analyticsQueryService;
    private final WorkspaceAccessService workspaceAccessService;

    public DashboardController(
            ExecutiveDashboardService executiveDashboardService,
            AnalyticsQueryService analyticsQueryService,
            WorkspaceAccessService workspaceAccessService
    ) {
        this.executiveDashboardService = executiveDashboardService;
        this.analyticsQueryService = analyticsQueryService;
        this.workspaceAccessService = workspaceAccessService;
    }

    @GetMapping("/executive")
    @Operation(summary = "Executive dashboard", description = "Overview across workspaces the user can access.")
    public ApiResponse<ExecutiveDashboardResponse> executive(@AuthenticationPrincipal AuthenticatedUser user) {
        return ApiResponse.of(executiveDashboardService.getExecutiveDashboard(user.getId()));
    }

    @GetMapping("/workspace/{id}")
    @Operation(summary = "Workspace dashboard alias", description = "Primary All Projects dashboard (same engine as /workspaces/{id}/dashboard).")
    public ApiResponse<ScopeDashboardResponse> workspace(
            @PathVariable UUID id,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        workspaceAccessService.requireAnalyticsAccess(id, user.getId());
        return ApiResponse.of(analyticsQueryService.getWorkspaceDashboard(id));
    }

    @GetMapping("/portfolio/{id}")
    @Operation(summary = "Portfolio dashboard alias")
    public ApiResponse<ScopeDashboardResponse> portfolio(
            @PathVariable UUID id,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        workspaceAccessService.requireAnalyticsAccessForPortfolio(id, user.getId());
        return ApiResponse.of(analyticsQueryService.getPortfolioDashboard(id));
    }

    @GetMapping("/project/{id}")
    @Operation(summary = "Project dashboard alias")
    public ApiResponse<?> project(
            @PathVariable UUID id,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        workspaceAccessService.requireAnalyticsAccessForProject(id, user.getId());
        return ApiResponse.of(analyticsQueryService.getProjectDashboard(id));
    }
}
