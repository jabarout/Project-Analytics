package com.projectanalytics.recommendation.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.projectanalytics.analytics.api.dto.ProjectAnalyticsResponse;
import com.projectanalytics.analytics.api.dto.ScopeAnalyticsKpiResponse;
import com.projectanalytics.analytics.api.dto.ScopeDashboardResponse;
import com.projectanalytics.analytics.api.dto.TrendPointResponse;
import com.projectanalytics.analytics.application.AnalyticsQueryService;
import com.projectanalytics.analytics.persistence.AnalyticsEntity;
import com.projectanalytics.analytics.persistence.AnalyticsRepository;
import com.projectanalytics.common.exception.BusinessException;
import com.projectanalytics.common.exception.ErrorCode;
import com.projectanalytics.portfolio.persistence.PortfolioEntity;
import com.projectanalytics.portfolio.persistence.PortfolioRepository;
import com.projectanalytics.project.persistence.ProjectEntity;
import com.projectanalytics.project.persistence.ProjectRepository;
import com.projectanalytics.recommendation.api.dto.RecommendationBundleResponse;
import com.projectanalytics.recommendation.api.dto.RecommendationResponse;
import com.projectanalytics.recommendation.api.dto.SupportingMetricResponse;
import com.projectanalytics.recommendation.application.rules.RecommendationCandidate;
import com.projectanalytics.recommendation.config.RecommendationProperties;
import com.projectanalytics.recommendation.persistence.RecommendationEntity;
import com.projectanalytics.recommendation.persistence.RecommendationRepository;
import com.projectanalytics.synchronization.persistence.WorkspaceEntity;
import com.projectanalytics.synchronization.persistence.WorkspaceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * Recommendation application service.
 * Consumes analytics query outputs only — never OpenProject and never scoring calculators.
 */
@Service
public class RecommendationService {

    private final RecommendationEngine recommendationEngine;
    private final RecommendationRepository recommendationRepository;
    private final AnalyticsQueryService analyticsQueryService;
    private final AnalyticsRepository analyticsRepository;
    private final ProjectRepository projectRepository;
    private final PortfolioRepository portfolioRepository;
    private final WorkspaceRepository workspaceRepository;
    private final RecommendationProperties properties;
    private final ObjectMapper objectMapper;

    public RecommendationService(
            RecommendationEngine recommendationEngine,
            RecommendationRepository recommendationRepository,
            AnalyticsQueryService analyticsQueryService,
            AnalyticsRepository analyticsRepository,
            ProjectRepository projectRepository,
            PortfolioRepository portfolioRepository,
            WorkspaceRepository workspaceRepository,
            RecommendationProperties properties,
            ObjectMapper objectMapper
    ) {
        this.recommendationEngine = recommendationEngine;
        this.recommendationRepository = recommendationRepository;
        this.analyticsQueryService = analyticsQueryService;
        this.analyticsRepository = analyticsRepository;
        this.projectRepository = projectRepository;
        this.portfolioRepository = portfolioRepository;
        this.workspaceRepository = workspaceRepository;
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public RecommendationBundleResponse getProjectRecommendations(UUID projectId) {
        ProjectEntity project = projectRepository.findById(projectId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_001));
        List<RecommendationResponse> recommendations = refreshProjectRecommendations(projectId);
        return new RecommendationBundleResponse(
                projectId,
                "PROJECT",
                project.getName(),
                buildProjectSummary(recommendations),
                recommendations
        );
    }

    @Transactional
    public RecommendationBundleResponse getWorkspaceRecommendations(UUID workspaceId) {
        WorkspaceEntity workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new BusinessException(ErrorCode.WORKSPACE_001));
        List<ProjectEntity> projects = projectRepository.findByWorkspaceIdOrderByNameAsc(workspaceId);
        List<RecommendationResponse> all = refreshProjects(projects);
        ScopeDashboardResponse dashboard = analyticsQueryService.getWorkspaceDashboard(workspaceId);
        all = limitScope(all);
        return new RecommendationBundleResponse(
                workspaceId,
                "WORKSPACE",
                workspace.getName(),
                buildScopeSummary(dashboard.kpis(), all),
                all
        );
    }

    @Transactional
    public RecommendationBundleResponse getPortfolioRecommendations(UUID portfolioId) {
        PortfolioEntity portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PORTFOLIO_001));
        List<ProjectEntity> projects = projectRepository.findMembersByPortfolioIdOrderByNameAsc(portfolioId);
        List<RecommendationResponse> all = refreshProjects(projects);
        ScopeDashboardResponse dashboard = analyticsQueryService.getPortfolioDashboard(portfolioId);
        all = limitScope(all);
        return new RecommendationBundleResponse(
                portfolioId,
                "PORTFOLIO",
                portfolio.getName(),
                buildScopeSummary(dashboard.kpis(), all),
                all
        );
    }

    @Transactional
    public RecommendationBundleResponse getExecutiveRecommendations() {
        List<WorkspaceEntity> workspaces = workspaceRepository.findAll();
        List<RecommendationResponse> all = new ArrayList<>();
        for (WorkspaceEntity workspace : workspaces) {
            List<ProjectEntity> projects = projectRepository.findByWorkspaceIdOrderByNameAsc(workspace.getId());
            all.addAll(refreshProjects(projects));
        }
        all = limitScope(all);
        String summary = all.isEmpty()
                ? "No high-priority recommendations across workspaces. Synchronize and recalculate analytics if data is missing."
                : "Top " + all.size() + " prioritized recommendations across "
                        + workspaces.size() + " workspace(s), ranked by severity then rule order.";
        return new RecommendationBundleResponse(
                null,
                "EXECUTIVE",
                "All workspaces",
                summary,
                all
        );
    }

    @Transactional(readOnly = true)
    public RecommendationResponse getRecommendation(UUID id) {
        RecommendationEntity entity = recommendationRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.RECOMMENDATION_001));
        String projectName = projectRepository.findById(entity.getProjectId())
                .map(ProjectEntity::getName)
                .orElse("Unknown project");
        return toResponse(entity, projectName);
    }

    private List<RecommendationResponse> refreshProjects(List<ProjectEntity> projects) {
        List<RecommendationResponse> all = new ArrayList<>();
        for (ProjectEntity project : projects) {
            all.addAll(refreshProjectRecommendations(project.getId()));
        }
        all.sort(Comparator
                .comparing((RecommendationResponse r) -> r.severity().ordinal())
                .thenComparing(RecommendationResponse::priorityRank)
                .thenComparing(RecommendationResponse::projectName, Comparator.nullsLast(String::compareToIgnoreCase)));
        return all;
    }

    private List<RecommendationResponse> refreshProjectRecommendations(UUID projectId) {
        try {
            ProjectAnalyticsResponse analytics = analyticsQueryService.getOrComputeProjectAnalytics(projectId);
            List<TrendPointResponse> trends = analyticsQueryService.getProjectTrends(projectId);
            AnalyticsEntity analyticsEntity = analyticsRepository.findByProjectId(projectId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.ANALYTICS_005));

            List<RecommendationCandidate> candidates =
                    recommendationEngine.evaluateProject(analyticsEntity.getId(), analytics, trends);

            recommendationRepository.deleteByProjectId(projectId);

            Instant generatedAt = Instant.now();
            List<RecommendationEntity> saved = new ArrayList<>();
            int rank = 1;
            for (RecommendationCandidate candidate : candidates) {
                RecommendationEntity entity = new RecommendationEntity(
                        candidate.analyticsId(),
                        candidate.projectId(),
                        candidate.ruleCode(),
                        candidate.title(),
                        candidate.description(),
                        candidate.severity(),
                        candidate.explanation(),
                        candidate.suggestedAction(),
                        rank++,
                        serializeMetrics(candidate.supportingMetrics()),
                        generatedAt
                );
                saved.add(recommendationRepository.save(entity));
            }
            return saved.stream()
                    .map(entity -> toResponse(entity, analytics.projectName()))
                    .toList();
        } catch (BusinessException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new BusinessException(ErrorCode.ANALYTICS_004, "Recommendation generation failed.", exception);
        }
    }

    private List<RecommendationResponse> limitScope(List<RecommendationResponse> recommendations) {
        int limit = Math.max(1, properties.getMaxPerScope());
        if (recommendations.size() <= limit) {
            return recommendations;
        }
        return List.copyOf(recommendations.subList(0, limit));
    }

    private String serializeMetrics(List<RecommendationCandidate.SupportingMetric> metrics) {
        try {
            return objectMapper.writeValueAsString(metrics);
        } catch (JsonProcessingException exception) {
            return "[]";
        }
    }

    private List<SupportingMetricResponse> deserializeMetrics(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            List<RecommendationCandidate.SupportingMetric> metrics =
                    objectMapper.readValue(json, new TypeReference<>() {
                    });
            return metrics.stream()
                    .map(m -> new SupportingMetricResponse(m.code(), m.label(), m.value()))
                    .toList();
        } catch (JsonProcessingException exception) {
            return List.of();
        }
    }

    private RecommendationResponse toResponse(RecommendationEntity entity, String projectName) {
        return new RecommendationResponse(
                entity.getId(),
                entity.getProjectId(),
                projectName,
                entity.getAnalyticsId(),
                entity.getRuleCode(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getSeverity(),
                entity.getExplanation(),
                entity.getSuggestedAction(),
                entity.getPriorityRank(),
                deserializeMetrics(entity.getSupportingMetrics()),
                entity.getGeneratedAt()
        );
    }

    private static String buildProjectSummary(List<RecommendationResponse> recommendations) {
        if (recommendations.isEmpty()) {
            return "No rule-based recommendations for this project at current analytics thresholds.";
        }
        long critical = recommendations.stream().filter(r -> r.severity().name().equals("CRITICAL")).count();
        return recommendations.size() + " recommendation(s) generated from current Health/Risk/Attention analytics"
                + (critical > 0 ? " including " + critical + " critical item(s)." : ".");
    }

    private static String buildScopeSummary(ScopeAnalyticsKpiResponse kpis, List<RecommendationResponse> recommendations) {
        return "Scope has " + kpis.criticalProjects() + " critical-health project(s) and "
                + kpis.highAttentionProjects() + " high-attention project(s). "
                + (recommendations.isEmpty()
                ? "No prioritized recommendations at current thresholds."
                : recommendations.size() + " prioritized recommendation(s) ready for action.");
    }
}
