package com.projectanalytics.recommendation.api;

import com.projectanalytics.common.api.ApiResponse;
import com.projectanalytics.common.constants.ApplicationConstants;
import com.projectanalytics.recommendation.api.dto.RecommendationBundleResponse;
import com.projectanalytics.recommendation.api.dto.RecommendationResponse;
import com.projectanalytics.recommendation.application.RecommendationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Recommendation endpoints. Module is independent of analytics scoring;
 * it only interprets analytics DTOs via {@link RecommendationService}.
 */
@RestController
@RequestMapping(ApplicationConstants.API_V1_BASE_PATH)
@Tag(name = "Recommendations", description = "Deterministic explainable recommendations from local analytics")
@SecurityRequirement(name = "bearerAuth")
public class RecommendationController {

    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @GetMapping("/projects/{id}/recommendations")
    @Operation(summary = "Project recommendations", description = "Rule-based recommendations for a project from current analytics.")
    public ApiResponse<RecommendationBundleResponse> projectRecommendations(@PathVariable UUID id) {
        return ApiResponse.of(recommendationService.getProjectRecommendations(id));
    }

    @GetMapping("/workspaces/{id}/recommendations")
    @Operation(summary = "Workspace recommendations", description = "Prioritized recommendations across all projects in a workspace.")
    public ApiResponse<RecommendationBundleResponse> workspaceRecommendations(@PathVariable UUID id) {
        return ApiResponse.of(recommendationService.getWorkspaceRecommendations(id));
    }

    @GetMapping("/portfolios/{id}/recommendations")
    @Operation(summary = "Portfolio recommendations", description = "Prioritized recommendations for portfolio member projects.")
    public ApiResponse<RecommendationBundleResponse> portfolioRecommendations(@PathVariable UUID id) {
        return ApiResponse.of(recommendationService.getPortfolioRecommendations(id));
    }

    @GetMapping("/recommendations/executive")
    @Operation(summary = "Executive recommendations", description = "Cross-workspace prioritized recommendation list.")
    public ApiResponse<RecommendationBundleResponse> executiveRecommendations() {
        return ApiResponse.of(recommendationService.getExecutiveRecommendations());
    }

    @GetMapping("/recommendations/{id}")
    @Operation(summary = "Recommendation details", description = "Returns a previously generated recommendation by id.")
    public ApiResponse<RecommendationResponse> getRecommendation(@PathVariable UUID id) {
        return ApiResponse.of(recommendationService.getRecommendation(id));
    }
}
