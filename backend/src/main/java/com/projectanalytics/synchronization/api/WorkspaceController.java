package com.projectanalytics.synchronization.api;

import com.projectanalytics.analytics.api.dto.ScopeAnalyticsKpiResponse;
import com.projectanalytics.analytics.api.dto.ScopeDashboardResponse;
import com.projectanalytics.analytics.application.AnalyticsQueryService;
import com.projectanalytics.common.api.ApiResponse;
import com.projectanalytics.common.constants.ApplicationConstants;
import com.projectanalytics.portfolio.api.dto.PortfolioProjectSummaryResponse;
import com.projectanalytics.portfolio.application.PortfolioService;
import com.projectanalytics.synchronization.api.dto.CreateWorkspaceRequest;
import com.projectanalytics.synchronization.api.dto.SynchronizationStatusResponse;
import com.projectanalytics.synchronization.api.dto.UpdateWorkspaceRequest;
import com.projectanalytics.synchronization.api.dto.WorkspaceResponse;
import com.projectanalytics.synchronization.application.SynchronizationResult;
import com.projectanalytics.synchronization.application.SynchronizationService;
import com.projectanalytics.synchronization.application.WorkspaceService;
import com.projectanalytics.synchronization.domain.SynchronizationType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Workspace and synchronization endpoints (API Specification §7).
 */
@RestController
@RequestMapping(ApplicationConstants.API_V1_BASE_PATH + "/workspaces")
@Tag(name = "Workspaces", description = "OpenProject workspace connection and synchronization")
@SecurityRequirement(name = "bearerAuth")
public class WorkspaceController {

    private final WorkspaceService workspaceService;
    private final SynchronizationService synchronizationService;
    private final AnalyticsQueryService analyticsQueryService;
    private final PortfolioService portfolioService;

    public WorkspaceController(
            WorkspaceService workspaceService,
            SynchronizationService synchronizationService,
            AnalyticsQueryService analyticsQueryService,
            PortfolioService portfolioService
    ) {
        this.workspaceService = workspaceService;
        this.synchronizationService = synchronizationService;
        this.analyticsQueryService = analyticsQueryService;
        this.portfolioService = portfolioService;
    }

    @GetMapping
    @Operation(summary = "List workspaces")
    public ApiResponse<List<WorkspaceResponse>> listWorkspaces() {
        return ApiResponse.of(workspaceService.listWorkspaces());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get workspace")
    public ApiResponse<WorkspaceResponse> getWorkspace(@PathVariable UUID id) {
        return ApiResponse.of(workspaceService.getWorkspace(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register workspace", description = "Creates a workspace connected to an OpenProject base URL.")
    public ApiResponse<WorkspaceResponse> createWorkspace(@Valid @RequestBody CreateWorkspaceRequest request) {
        return ApiResponse.of(workspaceService.createWorkspace(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Rename connection", description = "Updates the display name of an OpenProject connection.")
    public ApiResponse<WorkspaceResponse> updateWorkspace(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateWorkspaceRequest request
    ) {
        return ApiResponse.of(workspaceService.updateWorkspace(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Disconnect connection",
            description = "Removes the OpenProject connection and purges local synchronized data for that workspace."
    )
    public void deleteWorkspace(@PathVariable UUID id) {
        workspaceService.deleteWorkspace(id);
    }

    @PostMapping("/{id}/synchronize")
    @Operation(summary = "Synchronize workspace", description = "Starts a manual synchronization run.")
    public ApiResponse<SynchronizationStatusResponse> synchronize(@PathVariable UUID id) {
        return ApiResponse.of(toStatus(synchronizationService.synchronizeWorkspace(id, SynchronizationType.MANUAL)));
    }

    @GetMapping("/{id}/synchronization")
    @Operation(summary = "Synchronization status", description = "Returns the latest synchronization run for the workspace.")
    public ApiResponse<SynchronizationStatusResponse> synchronizationStatus(@PathVariable UUID id) {
        return ApiResponse.of(toStatus(synchronizationService.getLatestStatus(id)));
    }

    @GetMapping("/{id}/dashboard")
    @Operation(
            summary = "Workspace dashboard (primary analytics surface)",
            description = "All Projects analytics for the workspace using the shared analytics engine. Local data only."
    )
    public ApiResponse<ScopeDashboardResponse> dashboard(@PathVariable UUID id) {
        return ApiResponse.of(analyticsQueryService.getWorkspaceDashboard(id));
    }

    @GetMapping("/{id}/kpis")
    @Operation(summary = "Workspace KPIs", description = "Aggregated analytics KPIs for all projects in the workspace.")
    public ApiResponse<ScopeAnalyticsKpiResponse> kpis(@PathVariable UUID id) {
        return ApiResponse.of(analyticsQueryService.getWorkspaceKpis(id));
    }

    @GetMapping("/{id}/projects")
    @Operation(
            summary = "List workspace projects",
            description = "All synchronized projects owned by the workspace (for portfolio membership UI)."
    )
    public ApiResponse<List<PortfolioProjectSummaryResponse>> projects(@PathVariable UUID id) {
        return ApiResponse.of(portfolioService.listWorkspaceProjects(id));
    }

    private static SynchronizationStatusResponse toStatus(SynchronizationResult result) {
        return new SynchronizationStatusResponse(
                result.historyId(),
                result.workspaceId(),
                result.syncType() == null ? null : result.syncType().name(),
                result.status() == null ? null : result.status().name(),
                result.synchronizedProjects(),
                result.synchronizedWorkPackages(),
                result.startedAt(),
                result.finishedAt(),
                result.durationMs(),
                result.errorMessage()
        );
    }
}
