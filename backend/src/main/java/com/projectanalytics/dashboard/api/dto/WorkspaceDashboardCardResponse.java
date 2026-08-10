package com.projectanalytics.dashboard.api.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record WorkspaceDashboardCardResponse(
        UUID workspaceId,
        String workspaceName,
        String synchronizationStatus,
        long totalProjects,
        long activeProjects,
        long criticalProjects,
        long highAttentionProjects,
        BigDecimal averageHealthScore,
        BigDecimal averageRiskScore,
        BigDecimal averageAttentionScore
) {
}
