package com.projectanalytics.dashboard.api;

import com.projectanalytics.analytics.api.dto.ProjectAttentionSummaryResponse;
import com.projectanalytics.analytics.api.dto.ScopeDashboardResponse;
import com.projectanalytics.analytics.application.AnalyticsQueryService;
import com.projectanalytics.common.constants.ApplicationConstants;
import com.projectanalytics.dashboard.api.dto.ExecutiveDashboardResponse;
import com.projectanalytics.dashboard.api.dto.WorkspaceDashboardCardResponse;
import com.projectanalytics.dashboard.application.ExecutiveDashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * Lightweight CSV exports for dashboards (presentation/export only — not full reporting M7).
 */
@RestController
@RequestMapping(ApplicationConstants.API_V1_BASE_PATH + "/dashboards")
@Tag(name = "Dashboard Export", description = "CSV exports of existing dashboard data")
@SecurityRequirement(name = "bearerAuth")
public class DashboardExportController {

    private final ExecutiveDashboardService executiveDashboardService;
    private final AnalyticsQueryService analyticsQueryService;

    public DashboardExportController(
            ExecutiveDashboardService executiveDashboardService,
            AnalyticsQueryService analyticsQueryService
    ) {
        this.executiveDashboardService = executiveDashboardService;
        this.analyticsQueryService = analyticsQueryService;
    }

    @GetMapping(value = "/executive/export.csv", produces = "text/csv")
    @Operation(summary = "Export executive dashboard as CSV")
    public ResponseEntity<byte[]> exportExecutive() {
        ExecutiveDashboardResponse dashboard = executiveDashboardService.getExecutiveDashboard();
        StringBuilder csv = new StringBuilder();
        csv.append("workspaceId,workspaceName,syncStatus,totalProjects,activeProjects,criticalProjects,highAttention,avgHealth,avgRisk,avgAttention\n");
        for (WorkspaceDashboardCardResponse card : dashboard.workspaces()) {
            csv.append(card.workspaceId()).append(',')
                    .append(escape(card.workspaceName())).append(',')
                    .append(card.synchronizationStatus()).append(',')
                    .append(card.totalProjects()).append(',')
                    .append(card.activeProjects()).append(',')
                    .append(card.criticalProjects()).append(',')
                    .append(card.highAttentionProjects()).append(',')
                    .append(card.averageHealthScore()).append(',')
                    .append(card.averageRiskScore()).append(',')
                    .append(card.averageAttentionScore()).append('\n');
        }
        csv.append('\n').append("projectId,projectName,health,risk,attention\n");
        for (ProjectAttentionSummaryResponse project : dashboard.topAttentionProjects()) {
            csv.append(project.projectId()).append(',')
                    .append(escape(project.projectName())).append(',')
                    .append(project.healthScore()).append(',')
                    .append(project.riskScore()).append(',')
                    .append(project.attentionScore()).append('\n');
        }
        return csvResponse("executive-dashboard.csv", csv.toString());
    }

    @GetMapping(value = "/workspace/{id}/export.csv", produces = "text/csv")
    @Operation(summary = "Export workspace dashboard attention list as CSV")
    public ResponseEntity<byte[]> exportWorkspace(@PathVariable UUID id) {
        ScopeDashboardResponse dashboard = analyticsQueryService.getWorkspaceDashboard(id);
        StringBuilder csv = new StringBuilder();
        csv.append("projectId,projectName,status,healthScore,healthStatus,riskScore,riskLevel,attentionScore,attentionLabel\n");
        for (ProjectAttentionSummaryResponse project : dashboard.topAttentionProjects()) {
            csv.append(project.projectId()).append(',')
                    .append(escape(project.projectName())).append(',')
                    .append(escape(project.status())).append(',')
                    .append(project.healthScore()).append(',')
                    .append(escape(project.healthStatus())).append(',')
                    .append(project.riskScore()).append(',')
                    .append(escape(project.riskLevel())).append(',')
                    .append(project.attentionScore()).append(',')
                    .append(escape(project.attentionLabel())).append('\n');
        }
        return csvResponse("workspace-" + id + "-dashboard.csv", csv.toString());
    }

    private static ResponseEntity<byte[]> csvResponse(String filename, String body) {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(bytes);
    }

    private static String escape(String value) {
        if (value == null) {
            return "";
        }
        String escaped = value.replace("\"", "\"\"");
        if (escaped.contains(",") || escaped.contains("\n") || escaped.contains("\"")) {
            return "\"" + escaped + "\"";
        }
        return escaped;
    }
}
