package com.projectanalytics.analytics.api.dto;

import java.util.List;
import java.util.UUID;

/**
 * Shared dashboard DTO for workspace and portfolio scopes.
 */
public record ScopeDashboardResponse(
        UUID scopeId,
        String scopeType,
        String scopeName,
        UUID workspaceId,
        ScopeAnalyticsKpiResponse kpis,
        String executiveSummary,
        List<ProjectAttentionSummaryResponse> topAttentionProjects,
        List<ProjectAttentionSummaryResponse> criticalHealthProjects,
        List<String> insights
) {
}
