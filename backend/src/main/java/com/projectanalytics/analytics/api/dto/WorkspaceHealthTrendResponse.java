package com.projectanalytics.analytics.api.dto;

import java.util.List;

/**
 * Workspace Average Health over time + compact ranked Health drivers.
 * Aggregation matches existing averageHealthScore KPI — no new formula.
 */
public record WorkspaceHealthTrendResponse(
        List<WorkspaceHealthTrendPointResponse> points,
        List<ProjectHealthDriverResponse> improving,
        List<ProjectHealthDriverResponse> worsening
) {
}
