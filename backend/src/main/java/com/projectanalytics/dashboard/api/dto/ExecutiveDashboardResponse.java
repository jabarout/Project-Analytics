package com.projectanalytics.dashboard.api.dto;

import com.projectanalytics.analytics.api.dto.ProjectAttentionSummaryResponse;
import com.projectanalytics.analytics.api.dto.ScopeAnalyticsKpiResponse;

import java.util.List;

/**
 * Executive overview composed from existing analytics scope results.
 * No scoring formulas live here — only presentation aggregation.
 */
public record ExecutiveDashboardResponse(
        int workspaceCount,
        int portfolioCount,
        long totalProjects,
        long criticalProjects,
        long highAttentionProjects,
        List<WorkspaceDashboardCardResponse> workspaces,
        List<ProjectAttentionSummaryResponse> topAttentionProjects,
        List<String> insights,
        List<ScopeAnalyticsKpiResponse> workspaceKpis
) {
}
