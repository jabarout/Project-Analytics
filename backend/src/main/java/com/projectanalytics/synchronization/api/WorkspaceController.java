package com.projectanalytics.synchronization.api;

import com.projectanalytics.analytics.api.dto.ScopeAnalyticsKpiResponse;
import com.projectanalytics.analytics.api.dto.ScopeDashboardResponse;
import com.projectanalytics.analytics.application.AnalyticsQueryService;
import com.projectanalytics.common.api.ApiResponse;
import com.projectanalytics.common.constants.ApplicationConstants;
import com.projectanalytics.portfolio.api.dto.PortfolioProjectSummaryResponse;
import com.projectanalytics.portfolio.application.PortfolioService;
import com.projectanalytics.authentication.security.AuthenticatedUser;
import com.projectanalytics.synchronization.api.dto.ConnectWorkspaceApiKeyRequest;
import com.projectanalytics.synchronization.api.dto.CreateWorkspaceRequest;
import com.projectanalytics.synchronization.api.dto.GrantWorkspaceAccessRequest;
import com.projectanalytics.synchronization.api.dto.OAuthConnectStatusResponse;
import com.projectanalytics.synchronization.api.dto.StartOAuthConnectRequest;
import com.projectanalytics.synchronization.api.dto.StartOAuthConnectResponse;
import com.projectanalytics.synchronization.api.dto.SynchronizationStatusResponse;
import com.projectanalytics.synchronization.api.dto.UpdateWorkspaceRequest;
import com.projectanalytics.synchronization.api.dto.WorkspaceMemberResponse;
import com.projectanalytics.synchronization.api.dto.WorkspaceResponse;
import com.projectanalytics.synchronization.application.SynchronizationResult;
import com.projectanalytics.synchronization.application.SynchronizationService;
import com.projectanalytics.synchronization.application.WorkspaceAccessService;
import com.projectanalytics.synchronization.application.WorkspaceConnectionService;
import com.projectanalytics.synchronization.application.WorkspaceOAuthConnectService;
import com.projectanalytics.synchronization.application.WorkspaceService;
import com.projectanalytics.synchronization.domain.SynchronizationType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
    private final WorkspaceConnectionService workspaceConnectionService;
    private final WorkspaceOAuthConnectService workspaceOAuthConnectService;
    private final WorkspaceAccessService workspaceAccessService;
    private final SynchronizationService synchronizationService;
    private final AnalyticsQueryService analyticsQueryService;
    private final PortfolioService portfolioService;

    public WorkspaceController(
            WorkspaceService workspaceService,
            WorkspaceConnectionService workspaceConnectionService,
            WorkspaceOAuthConnectService workspaceOAuthConnectService,
            WorkspaceAccessService workspaceAccessService,
            SynchronizationService synchronizationService,
            AnalyticsQueryService analyticsQueryService,
            PortfolioService portfolioService
    ) {
        this.workspaceService = workspaceService;
        this.workspaceConnectionService = workspaceConnectionService;
        this.workspaceOAuthConnectService = workspaceOAuthConnectService;
        this.workspaceAccessService = workspaceAccessService;
        this.synchronizationService = synchronizationService;
        this.analyticsQueryService = analyticsQueryService;
        this.portfolioService = portfolioService;
    }

    @GetMapping
    @Operation(summary = "List workspaces", description = "Workspaces where the current user has analytics access.")
    public ApiResponse<List<WorkspaceResponse>> listWorkspaces(@AuthenticationPrincipal AuthenticatedUser user) {
        return ApiResponse.of(workspaceService.listWorkspaces(user.getId()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get workspace")
    public ApiResponse<WorkspaceResponse> getWorkspace(
            @PathVariable UUID id,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        return ApiResponse.of(workspaceService.getWorkspace(id, user.getId()));
    }

    @GetMapping("/oauth/status")
    @Operation(summary = "OAuth connect availability", description = "Whether OpenProject OAuth client credentials are configured.")
    public ApiResponse<OAuthConnectStatusResponse> oauthStatus() {
        return ApiResponse.of(workspaceOAuthConnectService.status());
    }

    @PostMapping("/oauth/start")
    @Operation(
            summary = "Start OpenProject OAuth connect",
            description = "Creates PKCE state and returns the OpenProject authorization URL. Preferred connect path when OAuth is configured."
    )
    public ApiResponse<StartOAuthConnectResponse> startOAuth(
            @Valid @RequestBody StartOAuthConnectRequest request,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        return ApiResponse.of(workspaceOAuthConnectService.start(user.getId(), request));
    }

    @GetMapping("/oauth/callback")
    @Operation(
            summary = "OpenProject OAuth callback",
            description = "Public redirect target. Exchanges code, runs the same eligibility check as API-key connect, stores encrypted tokens, grants Workspace Admin to the initiator."
    )
    public ResponseEntity<Void> oauthCallback(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String error,
            @RequestParam(value = "error_description", required = false) String errorDescription
    ) {
        String redirect = workspaceOAuthConnectService.completeCallback(code, state, error, errorDescription);
        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, redirect)
                .build();
    }

    @PostMapping("/connect/api-key")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Connect OpenProject with API key",
            description = "Alternative to OAuth. Verifies OP eligibility, stores encrypted API key server-side, and grants Workspace Admin + analytics access to the connector."
    )
    public ApiResponse<WorkspaceResponse> connectWithApiKey(
            @Valid @RequestBody ConnectWorkspaceApiKeyRequest request,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        UUID workspaceId = workspaceConnectionService.connectWithApiKey(user.getId(), request);
        return ApiResponse.of(workspaceService.getWorkspace(workspaceId, user.getId()));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.GONE)
    @Operation(
            summary = "Register workspace (removed)",
            description = "Legacy URL-only create is disabled (Phase 2). Use OAuth or POST /workspaces/connect/api-key."
    )
    public ApiResponse<WorkspaceResponse> createWorkspace(
            @Valid @RequestBody CreateWorkspaceRequest request,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        throw new com.projectanalytics.common.exception.BusinessException(
                com.projectanalytics.common.exception.ErrorCode.SYNC_005,
                "Legacy workspace create is disabled. Use POST /api/v1/workspaces/oauth/start "
                        + "or POST /api/v1/workspaces/connect/api-key."
        );
    }

    @PutMapping("/{id}")
    @Operation(summary = "Rename connection", description = "Updates the display name of an OpenProject connection.")
    public ApiResponse<WorkspaceResponse> updateWorkspace(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateWorkspaceRequest request,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        workspaceAccessService.requireWorkspaceAdmin(id, user.getId());
        return ApiResponse.of(workspaceService.updateWorkspace(id, request, user.getId()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Disconnect connection",
            description = "Removes the OpenProject connection and purges local synchronized data for that workspace."
    )
    public void deleteWorkspace(@PathVariable UUID id, @AuthenticationPrincipal AuthenticatedUser user) {
        workspaceAccessService.requireWorkspaceAdmin(id, user.getId());
        workspaceService.deleteWorkspace(id);
    }

    @GetMapping("/{id}/members")
    @Operation(
            summary = "List workspace members",
            description = "PA users with membership on this workspace. Workspace Admin only."
    )
    public ApiResponse<List<WorkspaceMemberResponse>> listMembers(
            @PathVariable UUID id,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        return ApiResponse.of(workspaceAccessService.listMembers(id, user.getId()));
    }

    @PostMapping("/{id}/members")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Grant analytics access",
            description = "Grants analytics access to an existing PA user by email. Does not promote Workspace Admin."
    )
    public ApiResponse<WorkspaceMemberResponse> grantMember(
            @PathVariable UUID id,
            @Valid @RequestBody GrantWorkspaceAccessRequest request,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        return ApiResponse.of(
                workspaceAccessService.grantAnalyticsAccessByEmail(id, user.getId(), request.email())
        );
    }

    @DeleteMapping("/{id}/members/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Revoke analytics access",
            description = "Removes analytics membership for a non-admin PA user. Workspace Admins cannot be revoked this way."
    )
    public void revokeMember(
            @PathVariable UUID id,
            @PathVariable UUID userId,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        workspaceAccessService.revokeAnalyticsAccess(id, user.getId(), userId);
    }

    @PostMapping("/{id}/synchronize")
    @Operation(summary = "Synchronize workspace", description = "Starts a manual synchronization run.")
    public ApiResponse<SynchronizationStatusResponse> synchronize(
            @PathVariable UUID id,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        workspaceAccessService.requireWorkspaceAdmin(id, user.getId());
        return ApiResponse.of(toStatus(synchronizationService.synchronizeWorkspace(id, SynchronizationType.MANUAL)));
    }

    @GetMapping("/{id}/synchronization")
    @Operation(summary = "Synchronization status", description = "Returns the latest synchronization run for the workspace.")
    public ApiResponse<SynchronizationStatusResponse> synchronizationStatus(
            @PathVariable UUID id,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        workspaceAccessService.requireAnalyticsAccess(id, user.getId());
        return ApiResponse.of(toStatus(synchronizationService.getLatestStatus(id)));
    }

    @GetMapping("/{id}/dashboard")
    @Operation(
            summary = "Workspace dashboard (primary analytics surface)",
            description = "All Projects analytics for the workspace using the shared analytics engine. Local data only."
    )
    public ApiResponse<ScopeDashboardResponse> dashboard(
            @PathVariable UUID id,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        workspaceAccessService.requireAnalyticsAccess(id, user.getId());
        return ApiResponse.of(analyticsQueryService.getWorkspaceDashboard(id));
    }

    @GetMapping("/{id}/kpis")
    @Operation(summary = "Workspace KPIs", description = "Aggregated analytics KPIs for all projects in the workspace.")
    public ApiResponse<ScopeAnalyticsKpiResponse> kpis(
            @PathVariable UUID id,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        workspaceAccessService.requireAnalyticsAccess(id, user.getId());
        return ApiResponse.of(analyticsQueryService.getWorkspaceKpis(id));
    }

    @GetMapping("/{id}/projects")
    @Operation(
            summary = "List workspace projects",
            description = "All synchronized projects owned by the workspace (for portfolio membership UI)."
    )
    public ApiResponse<List<PortfolioProjectSummaryResponse>> projects(
            @PathVariable UUID id,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        workspaceAccessService.requireAnalyticsAccess(id, user.getId());
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
