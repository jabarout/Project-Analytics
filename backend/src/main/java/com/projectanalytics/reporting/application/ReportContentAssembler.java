package com.projectanalytics.reporting.application;

import com.projectanalytics.analytics.api.dto.ProjectAttentionSummaryResponse;
import com.projectanalytics.analytics.api.dto.ProjectDashboardResponse;
import com.projectanalytics.analytics.api.dto.ScopeAnalyticsKpiResponse;
import com.projectanalytics.analytics.api.dto.ScopeDashboardResponse;
import com.projectanalytics.analytics.application.AnalyticsQueryService;
import com.projectanalytics.common.exception.BusinessException;
import com.projectanalytics.common.exception.ErrorCode;
import com.projectanalytics.dashboard.api.dto.ExecutiveDashboardResponse;
import com.projectanalytics.dashboard.api.dto.WorkspaceDashboardCardResponse;
import com.projectanalytics.dashboard.application.ExecutiveDashboardService;
import com.projectanalytics.recommendation.api.dto.RecommendationBundleResponse;
import com.projectanalytics.recommendation.api.dto.RecommendationResponse;
import com.projectanalytics.recommendation.application.RecommendationService;
import com.projectanalytics.reporting.domain.ReportScopeType;
import com.projectanalytics.reporting.domain.ReportType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Assembles report content from analytics, dashboard, and recommendation module DTOs.
 * Does not call OpenProject and does not recalculate scores.
 */
@Component
public class ReportContentAssembler {

    private final AnalyticsQueryService analyticsQueryService;
    private final ExecutiveDashboardService executiveDashboardService;
    private final RecommendationService recommendationService;

    public ReportContentAssembler(
            AnalyticsQueryService analyticsQueryService,
            ExecutiveDashboardService executiveDashboardService,
            RecommendationService recommendationService
    ) {
        this.analyticsQueryService = analyticsQueryService;
        this.executiveDashboardService = executiveDashboardService;
        this.recommendationService = recommendationService;
    }

    public ReportDocument assemble(
            ReportType reportType,
            ReportScopeType scopeType,
            UUID scopeId
    ) {
        Instant now = Instant.now();
        return switch (reportType) {
            case EXECUTIVE -> assembleExecutive(now);
            case PORTFOLIO -> assemblePortfolio(requireScopeId(scopeId, "PORTFOLIO"), now);
            case PROJECT -> assembleProject(requireScopeId(scopeId, "PROJECT"), now);
            case KPI -> assembleKpi(resolveScopeType(scopeType, scopeId, reportType), requireScopeId(scopeId, "KPI"), now);
            case RISK -> assembleRisk(resolveScopeType(scopeType, scopeId, reportType), requireScopeId(scopeId, "RISK"), now);
        };
    }

    private ReportDocument assembleExecutive(Instant generatedAt) {
        ExecutiveDashboardResponse dashboard = executiveDashboardService.getExecutiveDashboard();
        List<ReportDocument.ReportSection> sections = new ArrayList<>();

        sections.add(new ReportDocument.ReportSection(
                "Executive summary",
                List.of(
                        metric("Workspaces", dashboard.workspaceCount()),
                        metric("Portfolios", dashboard.portfolioCount()),
                        metric("Projects", dashboard.totalProjects()),
                        metric("Critical projects", dashboard.criticalProjects()),
                        metric("High attention projects", dashboard.highAttentionProjects())
                ),
                dashboard.insights(),
                null
        ));

        sections.add(new ReportDocument.ReportSection(
                "Workspace overview",
                List.of(),
                List.of(),
                workspaceTable(dashboard.workspaces())
        ));

        sections.add(new ReportDocument.ReportSection(
                "Top attention projects",
                List.of(),
                List.of(),
                attentionTable(dashboard.topAttentionProjects())
        ));

        RecommendationBundleResponse recommendations = recommendationService.getExecutiveRecommendations();
        sections.add(recommendationSection(recommendations));

        return new ReportDocument(
                "Executive Report",
                "Cross-workspace overview from local analytics",
                generatedAt,
                sections
        );
    }

    private ReportDocument assemblePortfolio(UUID portfolioId, Instant generatedAt) {
        ScopeDashboardResponse dashboard = analyticsQueryService.getPortfolioDashboard(portfolioId);
        return scopeDocument("Portfolio Report", dashboard, generatedAt, true);
    }

    private ReportDocument assembleProject(UUID projectId, Instant generatedAt) {
        ProjectDashboardResponse dashboard = analyticsQueryService.getProjectDashboard(projectId);
        List<ReportDocument.MetricLine> metrics = new ArrayList<>();
        metrics.add(metric("Project", dashboard.projectName()));
        metrics.add(metric("Status", dashboard.status()));
        metrics.add(metric("Progress", formatDecimal(dashboard.progress())));
        metrics.add(metric("Budget", formatDecimal(dashboard.budget())));
        metrics.add(metric("Start date", String.valueOf(dashboard.startDate())));
        metrics.add(metric("End date", String.valueOf(dashboard.endDate())));
        if (dashboard.analytics() != null) {
            metrics.add(metric("Health score", formatDecimal(dashboard.analytics().health().score())));
            metrics.add(metric("Health status", dashboard.analytics().health().label()));
            metrics.add(metric("Risk score", formatDecimal(dashboard.analytics().risk().score())));
            metrics.add(metric("Risk level", dashboard.analytics().risk().label()));
            metrics.add(metric("Attention score", formatDecimal(dashboard.analytics().attention().score())));
            metrics.add(metric("Attention label", dashboard.analytics().attention().label()));
        }

        List<String> explanations = new ArrayList<>();
        if (dashboard.analytics() != null) {
            explanations.add("Health: " + nullSafe(dashboard.analytics().health().explanation()));
            explanations.add("Risk: " + nullSafe(dashboard.analytics().risk().explanation()));
            explanations.add("Attention: " + nullSafe(dashboard.analytics().attention().explanation()));
        }

        RecommendationBundleResponse recommendations = recommendationService.getProjectRecommendations(projectId);
        List<ReportDocument.ReportSection> sections = new ArrayList<>();
        sections.add(new ReportDocument.ReportSection("Project analytics", metrics, explanations, null));
        sections.add(recommendationSection(recommendations));

        return new ReportDocument(
                "Project Report — " + dashboard.projectName(),
                "Project analytics from local store",
                generatedAt,
                sections
        );
    }

    private ReportDocument assembleKpi(ReportScopeType scopeType, UUID scopeId, Instant generatedAt) {
        ScopeDashboardResponse dashboard = loadScopeDashboard(scopeType, scopeId);
        ScopeAnalyticsKpiResponse kpis = dashboard.kpis();
        List<ReportDocument.MetricLine> metrics = List.of(
                metric("Scope", dashboard.scopeName()),
                metric("Scope type", dashboard.scopeType()),
                metric("Total projects", kpis.totalProjects()),
                metric("Active projects", kpis.activeProjects()),
                metric("Critical projects", kpis.criticalProjects()),
                metric("High attention", kpis.highAttentionProjects()),
                metric("Overdue projects", kpis.overdueProjects()),
                metric("Total work packages", kpis.totalWorkPackages()),
                metric("Average health", formatDecimal(kpis.averageHealthScore())),
                metric("Average risk", formatDecimal(kpis.averageRiskScore())),
                metric("Average attention", formatDecimal(kpis.averageAttentionScore())),
                metric("Average actual progress", formatDecimal(kpis.averageCompletion())),
                metric("Average expected progress", formatDecimal(kpis.averageExpectedProgress())),
                metric("Average progress gap", formatDecimal(kpis.averageProgressGap())),
                metric("Projects behind schedule", kpis.projectsBehindSchedule()),
                metric("Projects with overdue WPs", kpis.projectsWithOverdueWorkPackages()),
                metric("Average overdue ratio", formatDecimal(kpis.averageOverdueRatio())),
                metric("Total budget", formatDecimal(kpis.totalBudget())),
                metric("Last calculated", String.valueOf(kpis.lastCalculatedAt()))
        );
        return new ReportDocument(
                "KPI Report — " + dashboard.scopeName(),
                "Scope KPIs from analytics engine",
                generatedAt,
                List.of(new ReportDocument.ReportSection("Key performance indicators", metrics, dashboard.insights(), null))
        );
    }

    private ReportDocument assembleRisk(ReportScopeType scopeType, UUID scopeId, Instant generatedAt) {
        if (scopeType == ReportScopeType.PROJECT) {
            ProjectDashboardResponse dashboard = analyticsQueryService.getProjectDashboard(scopeId);
            List<ReportDocument.MetricLine> metrics = new ArrayList<>();
            metrics.add(metric("Project", dashboard.projectName()));
            if (dashboard.analytics() != null) {
                metrics.add(metric("Risk score", formatDecimal(dashboard.analytics().risk().score())));
                metrics.add(metric("Risk level", dashboard.analytics().risk().label()));
                metrics.add(metric("Health score", formatDecimal(dashboard.analytics().health().score())));
                metrics.add(metric("Attention score", formatDecimal(dashboard.analytics().attention().score())));
            }
            List<String> paragraphs = dashboard.analytics() == null
                    ? List.of("Analytics not available for this project.")
                    : List.of(nullSafe(dashboard.analytics().risk().explanation()));
            return new ReportDocument(
                    "Risk Report — " + dashboard.projectName(),
                    "Risk view from local analytics",
                    generatedAt,
                    List.of(new ReportDocument.ReportSection("Risk profile", metrics, paragraphs, null))
            );
        }

        ScopeDashboardResponse dashboard = loadScopeDashboard(scopeType, scopeId);
        List<ReportDocument.MetricLine> metrics = List.of(
                metric("Scope", dashboard.scopeName()),
                metric("Critical health projects", dashboard.kpis().criticalProjects()),
                metric("High attention projects", dashboard.kpis().highAttentionProjects()),
                metric("Average risk", formatDecimal(dashboard.kpis().averageRiskScore())),
                metric("Average attention", formatDecimal(dashboard.kpis().averageAttentionScore()))
        );
        return new ReportDocument(
                "Risk Report — " + dashboard.scopeName(),
                "Risk and attention focus from local analytics",
                generatedAt,
                List.of(
                        new ReportDocument.ReportSection("Risk summary", metrics, dashboard.insights(), null),
                        new ReportDocument.ReportSection(
                                "Critical health projects",
                                List.of(),
                                List.of(),
                                attentionTable(dashboard.criticalHealthProjects())
                        ),
                        new ReportDocument.ReportSection(
                                "Top attention projects",
                                List.of(),
                                List.of(),
                                attentionTable(dashboard.topAttentionProjects())
                        )
                )
        );
    }

    private ReportDocument scopeDocument(
            String titlePrefix,
            ScopeDashboardResponse dashboard,
            Instant generatedAt,
            boolean includeAttention
    ) {
        ScopeAnalyticsKpiResponse kpis = dashboard.kpis();
        List<ReportDocument.MetricLine> metrics = List.of(
                metric("Scope", dashboard.scopeName()),
                metric("Total projects", kpis.totalProjects()),
                metric("Active projects", kpis.activeProjects()),
                metric("Critical projects", kpis.criticalProjects()),
                metric("High attention", kpis.highAttentionProjects()),
                metric("Average health", formatDecimal(kpis.averageHealthScore())),
                metric("Average risk", formatDecimal(kpis.averageRiskScore())),
                metric("Average attention", formatDecimal(kpis.averageAttentionScore()))
        );

        List<ReportDocument.ReportSection> sections = new ArrayList<>();
        sections.add(new ReportDocument.ReportSection(
                "Summary",
                metrics,
                List.of(nullSafe(dashboard.executiveSummary())),
                null
        ));
        if (includeAttention) {
            sections.add(new ReportDocument.ReportSection(
                    "Top attention projects",
                    List.of(),
                    dashboard.insights(),
                    attentionTable(dashboard.topAttentionProjects())
            ));
        }
        if ("PORTFOLIO".equals(dashboard.scopeType())) {
            sections.add(recommendationSection(recommendationService.getPortfolioRecommendations(dashboard.scopeId())));
        } else if ("WORKSPACE".equals(dashboard.scopeType())) {
            sections.add(recommendationSection(recommendationService.getWorkspaceRecommendations(dashboard.scopeId())));
        }
        return new ReportDocument(titlePrefix + " — " + dashboard.scopeName(), "Local analytics composition", generatedAt, sections);
    }

    private static ReportDocument.ReportSection recommendationSection(RecommendationBundleResponse bundle) {
        List<String> paragraphs = new ArrayList<>();
        if (bundle.executiveSummary() != null && !bundle.executiveSummary().isBlank()) {
            paragraphs.add(bundle.executiveSummary());
        }
        List<String> headers = List.of("Severity", "Project", "Title", "Suggested action");
        List<List<String>> rows = bundle.recommendations().stream()
                .map(r -> List.of(
                        r.severity().name(),
                        nullSafe(r.projectName()),
                        nullSafe(r.title()),
                        nullSafe(r.suggestedAction())
                ))
                .toList();
        return new ReportDocument.ReportSection(
                "Recommendations",
                List.of(metric("Count", bundle.recommendations().size())),
                paragraphs,
                new ReportDocument.ReportTable(headers, rows)
        );
    }

    private ScopeDashboardResponse loadScopeDashboard(ReportScopeType scopeType, UUID scopeId) {
        return switch (scopeType) {
            case WORKSPACE -> analyticsQueryService.getWorkspaceDashboard(scopeId);
            case PORTFOLIO -> analyticsQueryService.getPortfolioDashboard(scopeId);
            case PROJECT -> throw new BusinessException(
                    ErrorCode.VALIDATION_003,
                    "PROJECT scope is not valid for scope dashboards; use report type PROJECT or RISK with PROJECT scope."
            );
        };
    }

    private static ReportScopeType resolveScopeType(ReportScopeType scopeType, UUID scopeId, ReportType reportType) {
        if (scopeType != null) {
            return scopeType;
        }
        if (scopeId == null) {
            throw new BusinessException(ErrorCode.VALIDATION_001, "scopeId is required for " + reportType + " reports.");
        }
        // KPI/RISK without explicit scope type default to workspace (primary analytics scope).
        return ReportScopeType.WORKSPACE;
    }

    private static UUID requireScopeId(UUID scopeId, String reportType) {
        if (scopeId == null) {
            throw new BusinessException(ErrorCode.VALIDATION_001, "scopeId is required for " + reportType + " reports.");
        }
        return scopeId;
    }

    private static ReportDocument.ReportTable workspaceTable(List<WorkspaceDashboardCardResponse> workspaces) {
        List<String> headers = List.of(
                "Workspace", "Sync", "Projects", "Active", "Critical", "High attention", "Avg health", "Avg risk", "Avg attention"
        );
        List<List<String>> rows = workspaces.stream()
                .map(card -> List.of(
                        nullSafe(card.workspaceName()),
                        nullSafe(card.synchronizationStatus()),
                        String.valueOf(card.totalProjects()),
                        String.valueOf(card.activeProjects()),
                        String.valueOf(card.criticalProjects()),
                        String.valueOf(card.highAttentionProjects()),
                        formatDecimal(card.averageHealthScore()),
                        formatDecimal(card.averageRiskScore()),
                        formatDecimal(card.averageAttentionScore())
                ))
                .toList();
        return new ReportDocument.ReportTable(headers, rows);
    }

    private static ReportDocument.ReportTable attentionTable(List<ProjectAttentionSummaryResponse> projects) {
        List<String> headers = List.of(
                "Project", "Status", "Health", "Health status", "Risk", "Risk level", "Attention", "Attention label"
        );
        List<List<String>> rows = projects.stream()
                .map(project -> List.of(
                        nullSafe(project.projectName()),
                        nullSafe(project.status()),
                        formatDecimal(project.healthScore()),
                        nullSafe(project.healthStatus()),
                        formatDecimal(project.riskScore()),
                        nullSafe(project.riskLevel()),
                        formatDecimal(project.attentionScore()),
                        nullSafe(project.attentionLabel())
                ))
                .toList();
        return new ReportDocument.ReportTable(headers, rows);
    }

    private static ReportDocument.MetricLine metric(String label, Object value) {
        return new ReportDocument.MetricLine(label, value == null ? "—" : String.valueOf(value));
    }

    private static String formatDecimal(BigDecimal value) {
        return value == null ? "—" : value.stripTrailingZeros().toPlainString();
    }

    private static String nullSafe(String value) {
        return value == null ? "—" : value;
    }
}
