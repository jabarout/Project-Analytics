package com.projectanalytics.portfolio.api;

import com.projectanalytics.common.api.ApiResponse;
import com.projectanalytics.common.constants.ApplicationConstants;
import com.projectanalytics.portfolio.api.dto.AssignProjectRequest;
import com.projectanalytics.portfolio.api.dto.BulkAssignProjectsRequest;
import com.projectanalytics.portfolio.api.dto.CreatePortfolioRequest;
import com.projectanalytics.analytics.api.dto.ScopeAnalyticsKpiResponse;
import com.projectanalytics.analytics.api.dto.ScopeDashboardResponse;
import com.projectanalytics.portfolio.api.dto.PortfolioDetailResponse;
import com.projectanalytics.portfolio.api.dto.PortfolioSummaryResponse;
import com.projectanalytics.portfolio.api.dto.UpdatePortfolioRequest;
import com.projectanalytics.portfolio.application.PortfolioService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Portfolio REST API. All operations use local synchronized data only.
 */
@RestController
@RequestMapping(ApplicationConstants.API_V1_BASE_PATH + "/portfolios")
@Tag(name = "Portfolios", description = "Local portfolio management, KPIs, and dashboards (no OpenProject calls)")
@SecurityRequirement(name = "bearerAuth")
public class PortfolioController {

    private final PortfolioService portfolioService;

    public PortfolioController(PortfolioService portfolioService) {
        this.portfolioService = portfolioService;
    }

    @GetMapping
    @Operation(summary = "List portfolios", description = "Optional workspaceId filter. Local database only.")
    public ApiResponse<List<PortfolioSummaryResponse>> list(
            @RequestParam(required = false) UUID workspaceId
    ) {
        return ApiResponse.of(portfolioService.listPortfolios(workspaceId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Portfolio details")
    public ApiResponse<PortfolioDetailResponse> get(@PathVariable UUID id) {
        return ApiResponse.of(portfolioService.getPortfolio(id));
    }

    @GetMapping("/{id}/kpis")
    @Operation(summary = "Portfolio KPIs", description = "Analytics-engine aggregates for portfolio members (local data).")
    public ApiResponse<ScopeAnalyticsKpiResponse> kpis(@PathVariable UUID id) {
        return ApiResponse.of(portfolioService.getKpis(id));
    }

    @GetMapping("/{id}/dashboard")
    @Operation(summary = "Portfolio dashboard", description = "Shared analytics engine scope dashboard for member projects.")
    public ApiResponse<ScopeDashboardResponse> dashboard(@PathVariable UUID id) {
        return ApiResponse.of(portfolioService.getDashboard(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create portfolio")
    public ApiResponse<PortfolioSummaryResponse> create(@Valid @RequestBody CreatePortfolioRequest request) {
        return ApiResponse.of(portfolioService.createPortfolio(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update portfolio")
    public ApiResponse<PortfolioSummaryResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdatePortfolioRequest request
    ) {
        return ApiResponse.of(portfolioService.updatePortfolio(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete portfolio", description = "Deletes the analytical collection; projects remain owned by the workspace.")
    public void delete(@PathVariable UUID id) {
        portfolioService.deletePortfolio(id);
    }

    @PostMapping("/{id}/projects")
    @Operation(
            summary = "Add project to portfolio",
            description = "Adds membership (many-to-many). Project stays in the workspace and may belong to other portfolios."
    )
    public ApiResponse<PortfolioDetailResponse> addProject(
            @PathVariable UUID id,
            @Valid @RequestBody AssignProjectRequest request
    ) {
        return ApiResponse.of(portfolioService.addProject(id, request));
    }

    @PostMapping("/{id}/projects/bulk")
    @Operation(
            summary = "Bulk-add projects to portfolio",
            description = "Adds many memberships at once (filter/select in UI). Idempotent per project id."
    )
    public ApiResponse<PortfolioDetailResponse> addProjectsBulk(
            @PathVariable UUID id,
            @Valid @RequestBody BulkAssignProjectsRequest request
    ) {
        return ApiResponse.of(portfolioService.addProjects(id, request.projectIds()));
    }

    @DeleteMapping("/{id}/projects/{projectId}")
    @Operation(
            summary = "Remove project from portfolio",
            description = "Removes membership only. Does not delete the project from the workspace."
    )
    public ApiResponse<PortfolioDetailResponse> removeProject(
            @PathVariable UUID id,
            @PathVariable UUID projectId
    ) {
        return ApiResponse.of(portfolioService.removeProject(id, projectId));
    }
}
