package com.projectanalytics.analytics.api;

import com.projectanalytics.analytics.api.dto.ExplorerProjectRowResponse;
import com.projectanalytics.analytics.api.dto.ProjectAnalyticsResponse;
import com.projectanalytics.analytics.api.dto.TrendPointResponse;
import com.projectanalytics.analytics.application.AnalyticsQueryService;
import com.projectanalytics.analytics.application.AnalyticsRecalculationService;
import com.projectanalytics.common.api.ApiResponse;
import com.projectanalytics.common.constants.ApplicationConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping(ApplicationConstants.API_V1_BASE_PATH + "/analytics")
@Tag(name = "Analytics", description = "Explainable Health/Risk/Attention scores from local data only")
@SecurityRequirement(name = "bearerAuth")
public class AnalyticsController {

    private final AnalyticsQueryService analyticsQueryService;
    private final AnalyticsRecalculationService recalculationService;

    public AnalyticsController(
            AnalyticsQueryService analyticsQueryService,
            AnalyticsRecalculationService recalculationService
    ) {
        this.analyticsQueryService = analyticsQueryService;
        this.recalculationService = recalculationService;
    }

    @GetMapping("/projects/{id}/health")
    @Operation(summary = "Project health score")
    public ApiResponse<Map<String, Object>> health(@PathVariable UUID id) {
        ProjectAnalyticsResponse analytics = analyticsQueryService.getOrComputeProjectAnalytics(id);
        return ApiResponse.of(Map.of(
                "projectId", analytics.projectId(),
                "health", analytics.health()
        ));
    }

    @GetMapping("/projects/{id}/risk")
    @Operation(summary = "Project risk score")
    public ApiResponse<Map<String, Object>> risk(@PathVariable UUID id) {
        ProjectAnalyticsResponse analytics = analyticsQueryService.getOrComputeProjectAnalytics(id);
        return ApiResponse.of(Map.of(
                "projectId", analytics.projectId(),
                "risk", analytics.risk()
        ));
    }

    @GetMapping("/projects/{id}/attention")
    @Operation(summary = "Project attention score")
    public ApiResponse<Map<String, Object>> attention(@PathVariable UUID id) {
        ProjectAnalyticsResponse analytics = analyticsQueryService.getOrComputeProjectAnalytics(id);
        return ApiResponse.of(Map.of(
                "projectId", analytics.projectId(),
                "attention", analytics.attention()
        ));
    }

    @GetMapping("/projects/{id}/kpis")
    @Operation(summary = "Project analytics KPI bundle")
    public ApiResponse<ProjectAnalyticsResponse> kpis(@PathVariable UUID id) {
        return ApiResponse.of(analyticsQueryService.getOrComputeProjectAnalytics(id));
    }

    @GetMapping("/projects/{id}/trends")
    @Operation(summary = "Project analytics trends")
    public ApiResponse<List<TrendPointResponse>> trends(@PathVariable UUID id) {
        return ApiResponse.of(analyticsQueryService.getProjectTrends(id));
    }

    @PostMapping("/workspaces/{workspaceId}/recalculate")
    @Operation(summary = "Recalculate analytics for all projects in a workspace")
    public ApiResponse<Map<String, Object>> recalculateWorkspace(@PathVariable UUID workspaceId) {
        int count = recalculationService.recalculateWorkspace(workspaceId);
        return ApiResponse.of(Map.of("workspaceId", workspaceId, "projectsScored", count));
    }

    @GetMapping("/workspaces/{workspaceId}/explorer-projects")
    @Operation(
            summary = "Explorer project rows",
            description = "Read model for Project Explorer: local project fields + stored analytics. Optional portfolio membership filter."
    )
    public ApiResponse<List<ExplorerProjectRowResponse>> explorerProjects(
            @PathVariable UUID workspaceId,
            @RequestParam(required = false) UUID portfolioId
    ) {
        return ApiResponse.of(analyticsQueryService.listExplorerProjects(workspaceId, portfolioId));
    }
}
