package com.projectanalytics.dashboard.application;

import com.projectanalytics.analytics.api.dto.ProjectAttentionSummaryResponse;
import com.projectanalytics.analytics.api.dto.ScopeAnalyticsKpiResponse;
import com.projectanalytics.analytics.api.dto.ScopeDashboardResponse;
import com.projectanalytics.analytics.application.AnalyticsQueryService;
import com.projectanalytics.dashboard.api.dto.ExecutiveDashboardResponse;
import com.projectanalytics.dashboard.api.dto.WorkspaceDashboardCardResponse;
import com.projectanalytics.portfolio.persistence.PortfolioRepository;
import com.projectanalytics.synchronization.persistence.WorkspaceEntity;
import com.projectanalytics.synchronization.persistence.WorkspaceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Composes executive dashboard views from existing analytics outputs.
 * Does not introduce new business scoring logic.
 */
@Service
public class ExecutiveDashboardService {

    private final WorkspaceRepository workspaceRepository;
    private final PortfolioRepository portfolioRepository;
    private final AnalyticsQueryService analyticsQueryService;

    public ExecutiveDashboardService(
            WorkspaceRepository workspaceRepository,
            PortfolioRepository portfolioRepository,
            AnalyticsQueryService analyticsQueryService
    ) {
        this.workspaceRepository = workspaceRepository;
        this.portfolioRepository = portfolioRepository;
        this.analyticsQueryService = analyticsQueryService;
    }

    @Transactional
    public ExecutiveDashboardResponse getExecutiveDashboard() {
        List<WorkspaceEntity> workspaces = workspaceRepository.findAll();
        List<WorkspaceDashboardCardResponse> cards = new ArrayList<>();
        List<ScopeAnalyticsKpiResponse> workspaceKpis = new ArrayList<>();
        List<ProjectAttentionSummaryResponse> attentionPool = new ArrayList<>();
        List<String> insights = new ArrayList<>();

        long totalProjects = 0;
        long criticalProjects = 0;
        long highAttentionProjects = 0;

        for (WorkspaceEntity workspace : workspaces) {
            ScopeDashboardResponse dashboard = analyticsQueryService.getWorkspaceDashboard(workspace.getId());
            ScopeAnalyticsKpiResponse kpis = dashboard.kpis();
            workspaceKpis.add(kpis);
            totalProjects += kpis.totalProjects();
            criticalProjects += kpis.criticalProjects();
            highAttentionProjects += kpis.highAttentionProjects();

            cards.add(new WorkspaceDashboardCardResponse(
                    workspace.getId(),
                    workspace.getName(),
                    workspace.getSynchronizationStatus().name(),
                    kpis.totalProjects(),
                    kpis.activeProjects(),
                    kpis.criticalProjects(),
                    kpis.highAttentionProjects(),
                    kpis.averageHealthScore(),
                    kpis.averageRiskScore(),
                    kpis.averageAttentionScore()
            ));

            attentionPool.addAll(dashboard.topAttentionProjects());
            insights.addAll(prefixInsights(workspace.getName(), dashboard.insights()));
        }

        List<ProjectAttentionSummaryResponse> topAttention = attentionPool.stream()
                .filter(p -> p.attentionScore() != null)
                .sorted(Comparator.comparing(ProjectAttentionSummaryResponse::attentionScore).reversed())
                .limit(10)
                .toList();

        if (workspaces.isEmpty()) {
            insights = List.of("Connect and synchronize an OpenProject workspace to populate the executive dashboard.");
        } else if (insights.isEmpty()) {
            insights = List.of("No elevated risks detected across connected workspaces.");
        }

        long portfolioCount = portfolioRepository.count();

        return new ExecutiveDashboardResponse(
                workspaces.size(),
                (int) portfolioCount,
                totalProjects,
                criticalProjects,
                highAttentionProjects,
                cards,
                topAttention,
                insights.stream().distinct().limit(12).toList(),
                workspaceKpis
        );
    }

    private static List<String> prefixInsights(String workspaceName, List<String> insights) {
        return insights.stream()
                .map(insight -> "[" + workspaceName + "] " + insight)
                .toList();
    }
}
