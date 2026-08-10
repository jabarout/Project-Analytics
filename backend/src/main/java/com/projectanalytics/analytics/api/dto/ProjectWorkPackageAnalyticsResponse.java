package com.projectanalytics.analytics.api.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Local work-package analytics for Project Detail (M11B). No scoring formulas.
 */
public record ProjectWorkPackageAnalyticsResponse(
        UUID projectId,
        long totalWorkPackages,
        long openWorkPackages,
        long completedWorkPackages,
        long overdueWorkPackages,
        long highPriorityOpen,
        long blockedWorkPackages,
        long inProgressWorkPackages,
        List<StatusCount> statusDistribution,
        List<OverdueWorkPackageRow> overdueWorkPackagesList,
        List<AssigneeBottleneckRow> assigneeBottlenecks
) {
    public record StatusCount(String status, long count) {
    }

    public record OverdueWorkPackageRow(
            UUID id,
            String subject,
            String status,
            String priority,
            String assignee,
            LocalDate dueDate
    ) {
    }

    public record AssigneeBottleneckRow(
            String assignee,
            long openCount,
            long overdueCount,
            long totalCount
    ) {
    }
}
