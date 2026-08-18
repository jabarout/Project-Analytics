package com.projectanalytics.analytics.application;

import com.projectanalytics.analytics.api.dto.ExplorerProjectRowResponse;
import com.projectanalytics.analytics.api.dto.ProjectAnalyticsResponse;
import com.projectanalytics.analytics.api.dto.ProjectAttentionSummaryResponse;
import com.projectanalytics.analytics.api.dto.ProjectDashboardResponse;
import com.projectanalytics.analytics.api.dto.ProjectWorkPackageAnalyticsResponse;
import com.projectanalytics.analytics.api.dto.ScoreFactorResponse;
import com.projectanalytics.analytics.api.dto.ScoredMetricResponse;
import com.projectanalytics.analytics.api.dto.ScopeAnalyticsKpiResponse;
import com.projectanalytics.analytics.api.dto.ScopeDashboardResponse;
import com.projectanalytics.analytics.api.dto.TrendPointResponse;
import com.projectanalytics.project.persistence.WorkPackageEntity;
import com.projectanalytics.analytics.domain.ScoreFactor;
import com.projectanalytics.analytics.domain.ScoredMetric;
import com.projectanalytics.analytics.persistence.AnalyticsEntity;
import com.projectanalytics.analytics.persistence.AnalyticsRepository;
import com.projectanalytics.analytics.persistence.AnalyticsSnapshotEntity;
import com.projectanalytics.analytics.persistence.AnalyticsSnapshotRepository;
import com.projectanalytics.analytics.scoring.AttentionScoreCalculator;
import com.projectanalytics.common.exception.BusinessException;
import com.projectanalytics.common.exception.ErrorCode;
import com.projectanalytics.portfolio.persistence.PortfolioEntity;
import com.projectanalytics.portfolio.persistence.PortfolioProjectEntity;
import com.projectanalytics.portfolio.persistence.PortfolioProjectRepository;
import com.projectanalytics.portfolio.persistence.PortfolioRepository;
import com.projectanalytics.project.persistence.ProjectEntity;
import com.projectanalytics.project.persistence.ProjectRepository;
import com.projectanalytics.project.persistence.WorkPackageRepository;
import com.projectanalytics.synchronization.persistence.WorkspaceEntity;
import com.projectanalytics.synchronization.persistence.WorkspaceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Read-side analytics and shared scope dashboards (workspace / portfolio / project).
 * All scoring formulas remain in the scoring package; this service aggregates stored results.
 */
@Service
public class AnalyticsQueryService {

    private static final double CRITICAL_HEALTH_THRESHOLD = 40.0;
    private static final double NEEDS_ATTENTION_THRESHOLD = 50.0;

    private final AnalyticsRepository analyticsRepository;
    private final AnalyticsSnapshotRepository snapshotRepository;
    private final ProjectRepository projectRepository;
    private final PortfolioRepository portfolioRepository;
    private final PortfolioProjectRepository portfolioProjectRepository;
    private final WorkspaceRepository workspaceRepository;
    private final WorkPackageRepository workPackageRepository;
    private final AnalyticsRecalculationService recalculationService;
    private final ScoreFactorSerializer factorSerializer;

    public AnalyticsQueryService(
            AnalyticsRepository analyticsRepository,
            AnalyticsSnapshotRepository snapshotRepository,
            ProjectRepository projectRepository,
            PortfolioRepository portfolioRepository,
            PortfolioProjectRepository portfolioProjectRepository,
            WorkspaceRepository workspaceRepository,
            WorkPackageRepository workPackageRepository,
            AnalyticsRecalculationService recalculationService,
            ScoreFactorSerializer factorSerializer
    ) {
        this.analyticsRepository = analyticsRepository;
        this.snapshotRepository = snapshotRepository;
        this.projectRepository = projectRepository;
        this.portfolioRepository = portfolioRepository;
        this.portfolioProjectRepository = portfolioProjectRepository;
        this.workspaceRepository = workspaceRepository;
        this.workPackageRepository = workPackageRepository;
        this.recalculationService = recalculationService;
        this.factorSerializer = factorSerializer;
    }

    /**
     * Explorer read model: projects with stored analytics and operational flags (no rescoring).
     */
    @Transactional
    public List<ExplorerProjectRowResponse> listExplorerProjects(UUID workspaceId, UUID portfolioId) {
        if (!workspaceRepository.existsById(workspaceId)) {
            throw new BusinessException(ErrorCode.WORKSPACE_001);
        }
        List<ProjectEntity> projects;
        if (portfolioId != null) {
            PortfolioEntity portfolio = portfolioRepository.findById(portfolioId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.PORTFOLIO_001));
            if (!portfolio.getWorkspace().getId().equals(workspaceId)) {
                throw new BusinessException(ErrorCode.PORTFOLIO_001);
            }
            projects = projectRepository.findMembersByPortfolioIdOrderByNameAsc(portfolioId);
        } else {
            projects = projectRepository.findByWorkspaceIdOrderByNameAsc(workspaceId);
        }
        ensureAnalyticsPresent(projects);

        Map<UUID, AnalyticsEntity> analyticsByProject = analyticsRepository.findAllByWorkspaceId(workspaceId).stream()
                .collect(Collectors.toMap(a -> a.getProject().getId(), Function.identity(), (a, b) -> a));

        Map<UUID, Long> overdueWpByProject = new HashMap<>();
        for (Object[] row : workPackageRepository.countOverdueOpenByProjectInWorkspace(workspaceId)) {
            overdueWpByProject.put((UUID) row[0], (Long) row[1]);
        }

        // Earliest open WP due dates (fallback when project.end_date is null).
        Map<UUID, LocalDate> earliestOpenDueByProject = new HashMap<>();
        Map<UUID, LocalDate> earliestFutureOpenDueByProject = new HashMap<>();
        List<UUID> projectIds = projects.stream().map(ProjectEntity::getId).toList();
        if (!projectIds.isEmpty()) {
            LocalDate todayForDue = LocalDate.now();
            for (WorkPackageEntity wp : workPackageRepository.findByProjectIdIn(projectIds)) {
                if (wp.getDueDate() == null || isCompletedStatus(wp.getStatus())) {
                    continue;
                }
                UUID pid = wp.getProject().getId();
                LocalDate due = wp.getDueDate();
                LocalDate currentAny = earliestOpenDueByProject.get(pid);
                if (currentAny == null || due.isBefore(currentAny)) {
                    earliestOpenDueByProject.put(pid, due);
                }
                if (!due.isBefore(todayForDue)) {
                    LocalDate currentFuture = earliestFutureOpenDueByProject.get(pid);
                    if (currentFuture == null || due.isBefore(currentFuture)) {
                        earliestFutureOpenDueByProject.put(pid, due);
                    }
                }
            }
        }

        Map<UUID, List<UUID>> portfolioIdsByProject = new HashMap<>();
        Map<UUID, List<String>> portfolioNamesByProject = new HashMap<>();
        Map<UUID, String> portfolioNameById = portfolioRepository.findByWorkspaceIdOrderByNameAsc(workspaceId).stream()
                .collect(Collectors.toMap(PortfolioEntity::getId, PortfolioEntity::getName, (a, b) -> a, LinkedHashMap::new));
        for (PortfolioEntity portfolio : portfolioRepository.findByWorkspaceIdOrderByNameAsc(workspaceId)) {
            for (PortfolioProjectEntity membership : portfolioProjectRepository.findByPortfolioId(portfolio.getId())) {
                portfolioIdsByProject
                        .computeIfAbsent(membership.getProjectId(), ignored -> new ArrayList<>())
                        .add(portfolio.getId());
                portfolioNamesByProject
                        .computeIfAbsent(membership.getProjectId(), ignored -> new ArrayList<>())
                        .add(portfolioNameById.getOrDefault(portfolio.getId(), portfolio.getName()));
            }
        }

        LocalDate today = LocalDate.now();
        List<ExplorerProjectRowResponse> rows = new ArrayList<>(projects.size());
        for (ProjectEntity project : projects) {
            AnalyticsEntity analytics = analyticsByProject.get(project.getId());
            BigDecimal health = analytics == null ? null : analytics.getHealthScore();
            BigDecimal risk = analytics == null ? null : analytics.getRiskScore();
            BigDecimal attention = analytics == null ? null : analytics.getAttentionScore();
            boolean delayed = project.getEndDate() != null && project.getEndDate().isBefore(today);
            boolean critical = health != null && health.doubleValue() < CRITICAL_HEALTH_THRESHOLD;
            boolean needsAttention = attention != null && attention.doubleValue() >= NEEDS_ATTENTION_THRESHOLD;

            // Deadline used for "upcoming" filters:
            // 1) project finish date when present
            // 2) else earliest *future/today* open WP due date
            // 3) else earliest open WP due date (may be past — still useful signal)
            LocalDate nextDeadline;
            String nextDeadlineSource;
            if (project.getEndDate() != null) {
                nextDeadline = project.getEndDate();
                nextDeadlineSource = "project";
            } else if (earliestFutureOpenDueByProject.containsKey(project.getId())) {
                nextDeadline = earliestFutureOpenDueByProject.get(project.getId());
                nextDeadlineSource = "work_package";
            } else if (earliestOpenDueByProject.containsKey(project.getId())) {
                nextDeadline = earliestOpenDueByProject.get(project.getId());
                nextDeadlineSource = "work_package";
            } else {
                nextDeadline = null;
                nextDeadlineSource = null;
            }

            // Canonical progress = analytics completion % (WP-based when WPs exist).
            BigDecimal progressForUi = analytics != null && analytics.getCompletionPercentage() != null
                    ? analytics.getCompletionPercentage()
                    : project.getProgress();

            rows.add(new ExplorerProjectRowResponse(
                    project.getId(),
                    workspaceId,
                    project.getName(),
                    project.getStatus(),
                    progressForUi,
                    analytics == null ? null : analytics.getExpectedProgress(),
                    analytics == null ? null : analytics.getProgressGap(),
                    project.getBudget(),
                    project.getStartDate(),
                    project.getEndDate(),
                    project.getSynchronizedAt(),
                    health,
                    analytics == null ? null : analytics.getHealthStatus(),
                    risk,
                    analytics == null ? null : analytics.getRiskLevel(),
                    attention,
                    analytics == null ? null : analytics.getAttentionExplanation() == null
                            ? null
                            : attentionBandLabel(attention),
                    delayed,
                    critical,
                    needsAttention,
                    overdueWpByProject.getOrDefault(project.getId(), 0L),
                    analytics == null ? null : analytics.getOverdueRatio(),
                    analytics == null ? null : analytics.getScheduleVariance(),
                    List.copyOf(portfolioIdsByProject.getOrDefault(project.getId(), List.of())),
                    List.copyOf(portfolioNamesByProject.getOrDefault(project.getId(), List.of())),
                    project.getAdminName(),
                    nextDeadline,
                    nextDeadlineSource
            ));
        }
        return rows;
    }

    private static String attentionBandLabel(BigDecimal attention) {
        if (attention == null) {
            return null;
        }
        double value = attention.doubleValue();
        if (value >= NEEDS_ATTENTION_THRESHOLD) {
            return "Needs Attention";
        }
        if (value >= 30) {
            return "Watch";
        }
        return "Stable";
    }

    @Transactional(readOnly = true)
    public ProjectAnalyticsResponse getProjectAnalytics(UUID projectId) {
        ProjectEntity project = requireProject(projectId);
        AnalyticsEntity analytics = analyticsRepository.findByProjectId(projectId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ANALYTICS_005));
        return toProjectAnalyticsResponse(project, analytics);
    }

    @Transactional
    public ProjectAnalyticsResponse getOrComputeProjectAnalytics(UUID projectId) {
        return analyticsRepository.findByProjectId(projectId)
                .map(entity -> toProjectAnalyticsResponse(requireProject(projectId), entity))
                .orElseGet(() -> {
                    var snapshot = recalculationService.recalculateProject(projectId);
                    ProjectEntity project = requireProject(projectId);
                    return toProjectAnalyticsResponse(project, snapshot);
                });
    }

    @Transactional(readOnly = true)
    public List<TrendPointResponse> getProjectTrends(UUID projectId) {
        if (!projectRepository.existsById(projectId)) {
            throw new BusinessException(ErrorCode.PROJECT_001);
        }
        List<AnalyticsSnapshotEntity> snapshots =
                snapshotRepository.findTop20ByProjectIdOrderByCalculatedAtDesc(projectId);
        List<TrendPointResponse> points = new ArrayList<>();
        for (int i = snapshots.size() - 1; i >= 0; i--) {
            AnalyticsSnapshotEntity s = snapshots.get(i);
            points.add(new TrendPointResponse(
                    s.getCalculatedAt(),
                    s.getHealthScore(),
                    s.getRiskScore(),
                    s.getAttentionScore(),
                    s.getCompletionPercentage()
            ));
        }
        return points;
    }

    /**
     * Work-package operational analytics for Project Detail (local data only).
     */
    @Transactional(readOnly = true)
    public ProjectWorkPackageAnalyticsResponse getProjectWorkPackageAnalytics(UUID projectId) {
        requireProject(projectId);
        List<WorkPackageEntity> workPackages = workPackageRepository.findByProjectId(projectId);
        LocalDate today = LocalDate.now();

        long open = 0;
        long completed = 0;
        long overdue = 0;
        long highPriorityOpen = 0;
        long blocked = 0;
        long inProgress = 0;

        Map<String, Long> statusCounts = new LinkedHashMap<>();
        List<ProjectWorkPackageAnalyticsResponse.OverdueWorkPackageRow> overdueRows = new ArrayList<>();
        Map<String, long[]> assigneeStats = new HashMap<>(); // [open, overdue, total]

        for (WorkPackageEntity wp : workPackages) {
            String status = wp.getStatus() == null ? "Unknown" : wp.getStatus().trim();
            String statusKey = status.isEmpty() ? "Unknown" : status;
            statusCounts.merge(statusKey, 1L, Long::sum);

            boolean isCompleted = isCompletedStatus(wp.getStatus());
            boolean isBlocked = isBlockedStatus(wp.getStatus());
            boolean isInProgress = isInProgressStatus(wp.getStatus());
            boolean isOverdue = !isCompleted
                    && wp.getDueDate() != null
                    && wp.getDueDate().isBefore(today);

            if (isCompleted) {
                completed++;
            } else {
                open++;
                if (isHighPriority(wp.getPriority())) {
                    highPriorityOpen++;
                }
            }
            if (isOverdue) {
                overdue++;
                overdueRows.add(new ProjectWorkPackageAnalyticsResponse.OverdueWorkPackageRow(
                        wp.getId(),
                        wp.getSubject(),
                        wp.getStatus(),
                        wp.getPriority(),
                        wp.getAssignee(),
                        wp.getDueDate()
                ));
            }
            if (isBlocked) {
                blocked++;
            }
            if (isInProgress) {
                inProgress++;
            }

            String assignee = (wp.getAssignee() == null || wp.getAssignee().isBlank())
                    ? "Unassigned"
                    : wp.getAssignee().trim();
            long[] stats = assigneeStats.computeIfAbsent(assignee, ignored -> new long[3]);
            stats[2]++; // total
            if (!isCompleted) {
                stats[0]++; // open
            }
            if (isOverdue) {
                stats[1]++; // overdue
            }
        }

        List<ProjectWorkPackageAnalyticsResponse.StatusCount> distribution = statusCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .map(e -> new ProjectWorkPackageAnalyticsResponse.StatusCount(e.getKey(), e.getValue()))
                .toList();

        // Also expose synthetic buckets for the UI chart (Open / In Progress / Completed / Overdue / Blocked)
        // Overdue is a cross-cutting flag; chart can use both raw and synthetic — frontend prefers synthetic.

        overdueRows.sort(Comparator.comparing(
                ProjectWorkPackageAnalyticsResponse.OverdueWorkPackageRow::dueDate,
                Comparator.nullsLast(Comparator.naturalOrder())
        ));

        List<ProjectWorkPackageAnalyticsResponse.AssigneeBottleneckRow> bottlenecks = assigneeStats.entrySet().stream()
                .map(e -> new ProjectWorkPackageAnalyticsResponse.AssigneeBottleneckRow(
                        e.getKey(),
                        e.getValue()[0],
                        e.getValue()[1],
                        e.getValue()[2]
                ))
                .sorted(Comparator
                        .comparingLong(ProjectWorkPackageAnalyticsResponse.AssigneeBottleneckRow::overdueCount)
                        .reversed()
                        .thenComparing(
                                Comparator.comparingLong(
                                        ProjectWorkPackageAnalyticsResponse.AssigneeBottleneckRow::openCount
                                ).reversed()
                        ))
                .limit(25)
                .toList();

        return new ProjectWorkPackageAnalyticsResponse(
                projectId,
                workPackages.size(),
                open,
                completed,
                overdue,
                highPriorityOpen,
                blocked,
                inProgress,
                distribution,
                overdueRows,
                bottlenecks
        );
    }

    private static boolean isCompletedStatus(String status) {
        if (status == null || status.isBlank()) {
            return false;
        }
        String n = status.trim().toUpperCase(Locale.ROOT);
        return n.contains("CLOSED") || n.contains("DONE") || n.contains("RESOLVED")
                || n.contains("REJECTED") || n.contains("COMPLETED");
    }

    private static boolean isBlockedStatus(String status) {
        if (status == null || status.isBlank()) {
            return false;
        }
        String n = status.trim().toUpperCase(Locale.ROOT);
        return n.contains("BLOCK") || n.contains("ON HOLD") || n.contains("WAITING");
    }

    private static boolean isInProgressStatus(String status) {
        if (status == null || status.isBlank()) {
            return false;
        }
        String n = status.trim().toUpperCase(Locale.ROOT);
        return n.contains("PROGRESS") || n.contains("IN DEVELOPMENT") || n.contains("STARTED");
    }

    private static boolean isHighPriority(String priority) {
        if (priority == null || priority.isBlank()) {
            return false;
        }
        String n = priority.trim().toUpperCase(Locale.ROOT);
        return n.contains("HIGH") || n.contains("IMMEDIATE") || n.contains("URGENT");
    }

    @Transactional
    public ProjectDashboardResponse getProjectDashboard(UUID projectId) {
        ProjectEntity project = requireProject(projectId);
        ProjectAnalyticsResponse analytics = getOrComputeProjectAnalytics(projectId);
        // Progress on the dashboard is the same canonical completion % as Explorer / analytics.
        BigDecimal progressForUi = analytics.completionPercentage() != null
                ? analytics.completionPercentage()
                : project.getProgress();
        return new ProjectDashboardResponse(
                project.getId(),
                project.getName(),
                project.getWorkspace().getId(),
                null,
                project.getStatus(),
                progressForUi,
                project.getBudget(),
                project.getStartDate(),
                project.getEndDate(),
                project.getSynchronizedAt(),
                analytics,
                getProjectTrends(projectId)
        );
    }

    @Transactional
    public ScopeDashboardResponse getWorkspaceDashboard(UUID workspaceId) {
        WorkspaceEntity workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new BusinessException(ErrorCode.WORKSPACE_001));
        List<ProjectEntity> projects = projectRepository.findByWorkspaceIdOrderByNameAsc(workspaceId);
        ensureAnalyticsPresent(projects);
        List<AnalyticsEntity> analytics = analyticsRepository.findAllByWorkspaceId(workspaceId);
        return buildScopeDashboard(
                workspaceId,
                "WORKSPACE",
                workspace.getName(),
                workspaceId,
                projects,
                analytics
        );
    }

    @Transactional
    public ScopeAnalyticsKpiResponse getWorkspaceKpis(UUID workspaceId) {
        if (!workspaceRepository.existsById(workspaceId)) {
            throw new BusinessException(ErrorCode.WORKSPACE_001);
        }
        List<ProjectEntity> projects = projectRepository.findByWorkspaceIdOrderByNameAsc(workspaceId);
        ensureAnalyticsPresent(projects);
        List<AnalyticsEntity> analytics = analyticsRepository.findAllByWorkspaceId(workspaceId);
        return buildScopeKpis(workspaceId, "WORKSPACE", projects, analytics);
    }

    @Transactional
    public ScopeDashboardResponse getPortfolioDashboard(UUID portfolioId) {
        PortfolioEntity portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PORTFOLIO_001));
        List<ProjectEntity> projects = projectRepository.findMembersByPortfolioIdOrderByNameAsc(portfolioId);
        ensureAnalyticsPresent(projects);
        List<AnalyticsEntity> analytics = analyticsRepository.findAllByPortfolioId(portfolioId);
        return buildScopeDashboard(
                portfolioId,
                "PORTFOLIO",
                portfolio.getName(),
                portfolio.getWorkspace().getId(),
                projects,
                analytics
        );
    }

    @Transactional
    public ScopeAnalyticsKpiResponse getPortfolioKpis(UUID portfolioId) {
        if (!portfolioRepository.existsById(portfolioId)) {
            throw new BusinessException(ErrorCode.PORTFOLIO_001);
        }
        List<ProjectEntity> projects = projectRepository.findMembersByPortfolioIdOrderByNameAsc(portfolioId);
        ensureAnalyticsPresent(projects);
        List<AnalyticsEntity> analytics = analyticsRepository.findAllByPortfolioId(portfolioId);
        return buildScopeKpis(portfolioId, "PORTFOLIO", projects, analytics);
    }

    private void ensureAnalyticsPresent(List<ProjectEntity> projects) {
        for (ProjectEntity project : projects) {
            if (analyticsRepository.findByProjectId(project.getId()).isEmpty()) {
                recalculationService.recalculateProject(project.getId());
            }
        }
    }

    private ScopeDashboardResponse buildScopeDashboard(
            UUID scopeId,
            String scopeType,
            String scopeName,
            UUID workspaceId,
            List<ProjectEntity> projects,
            List<AnalyticsEntity> analytics
    ) {
        ScopeAnalyticsKpiResponse kpis = buildScopeKpis(scopeId, scopeType, projects, analytics);
        Map<UUID, AnalyticsEntity> byProject = analytics.stream()
                .collect(Collectors.toMap(a -> a.getProject().getId(), Function.identity(), (a, b) -> a));

        List<ProjectAttentionSummaryResponse> summaries = projects.stream()
                .map(project -> toAttentionSummary(project, byProject.get(project.getId())))
                .toList();

        List<ProjectAttentionSummaryResponse> topAttention = summaries.stream()
                .sorted(Comparator.comparing(
                        (ProjectAttentionSummaryResponse s) -> s.attentionScore() == null
                                ? BigDecimal.ZERO
                                : s.attentionScore()
                ).reversed())
                .limit(5)
                .toList();

        List<ProjectAttentionSummaryResponse> critical = summaries.stream()
                .filter(s -> s.healthScore() != null && s.healthScore().doubleValue() < 40)
                .sorted(Comparator.comparing(ProjectAttentionSummaryResponse::healthScore))
                .limit(5)
                .toList();

        String summary = buildExecutiveSummary(scopeType, scopeName, kpis);
        List<String> insights = buildInsights(kpis);

        return new ScopeDashboardResponse(
                scopeId,
                scopeType,
                scopeName,
                workspaceId,
                kpis,
                summary,
                topAttention,
                critical,
                insights
        );
    }

    private ScopeAnalyticsKpiResponse buildScopeKpis(
            UUID scopeId,
            String scopeType,
            List<ProjectEntity> projects,
            List<AnalyticsEntity> analytics
    ) {
        long total = projects.size();
        // Active = not archived. Status may be "ACTIVE", "On track", "At risk", etc.
        long active = projects.stream().filter(ProjectEntity::isActiveLifecycle).count();
        long overdueProjects = projects.stream().filter(this::isProjectOverdue).count();
        long totalWp = projects.stream()
                .mapToLong(p -> workPackageRepository.findByProjectId(p.getId()).size())
                .sum();

        long critical = analytics.stream()
                .filter(a -> a.getHealthScore() != null && a.getHealthScore().doubleValue() < 40)
                .count();
        long highAttention = analytics.stream()
                .filter(a -> a.getAttentionScore() != null && a.getAttentionScore().doubleValue() >= 50)
                .count();

        BigDecimal avgHealth = average(analytics, AnalyticsEntity::getHealthScore);
        BigDecimal avgRisk = average(analytics, AnalyticsEntity::getRiskScore);
        BigDecimal avgAttention = average(analytics, AnalyticsEntity::getAttentionScore);
        BigDecimal avgCompletion = average(analytics, AnalyticsEntity::getCompletionPercentage);
        // Averages of stored ProgressMetrics outputs — do not recompute schedule math here.
        BigDecimal avgExpected = average(analytics, AnalyticsEntity::getExpectedProgress);
        BigDecimal avgGap = average(analytics, AnalyticsEntity::getProgressGap);
        long behindSchedule = analytics.stream()
                .filter(a -> a.getProgressGap() != null && a.getProgressGap().compareTo(BigDecimal.ZERO) < 0)
                .count();
        BigDecimal avgOverdueRatio = average(analytics, AnalyticsEntity::getOverdueRatio);
        long withOverdueWp = analytics.stream()
                .filter(a -> a.getOverdueRatio() != null && a.getOverdueRatio().compareTo(BigDecimal.ZERO) > 0)
                .count();
        BigDecimal totalBudget = projects.stream()
                .map(ProjectEntity::getBudget)
                .filter(b -> b != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
        Instant lastCalc = analytics.stream()
                .map(AnalyticsEntity::getCalculatedAt)
                .filter(i -> i != null)
                .max(Instant::compareTo)
                .orElse(null);

        return new ScopeAnalyticsKpiResponse(
                scopeId,
                scopeType,
                total,
                active,
                critical,
                highAttention,
                overdueProjects,
                totalWp,
                avgHealth,
                avgRisk,
                avgAttention,
                avgCompletion,
                avgExpected,
                avgGap,
                behindSchedule,
                avgOverdueRatio,
                withOverdueWp,
                totalBudget,
                lastCalc
        );
    }

    private ProjectAttentionSummaryResponse toAttentionSummary(ProjectEntity project, AnalyticsEntity analytics) {
        if (analytics == null) {
            return new ProjectAttentionSummaryResponse(
                    project.getId(),
                    project.getName(),
                    project.getStatus(),
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            );
        }
        return new ProjectAttentionSummaryResponse(
                project.getId(),
                project.getName(),
                project.getStatus(),
                analytics.getHealthScore(),
                analytics.getHealthStatus(),
                analytics.getRiskScore(),
                analytics.getRiskLevel(),
                analytics.getAttentionScore(),
                AttentionScoreCalculator.attentionLabel(analytics.getAttentionScore())
        );
    }

    private ProjectAnalyticsResponse toProjectAnalyticsResponse(ProjectEntity project, AnalyticsEntity analytics) {
        return new ProjectAnalyticsResponse(
                project.getId(),
                project.getName(),
                new ScoredMetricResponse(
                        analytics.getHealthScore(),
                        analytics.getHealthStatus(),
                        analytics.getHealthExplanation(),
                        factorSerializer.deserialize(analytics.getHealthFactorsJson())
                ),
                new ScoredMetricResponse(
                        analytics.getRiskScore(),
                        analytics.getRiskLevel(),
                        analytics.getRiskExplanation(),
                        factorSerializer.deserialize(analytics.getRiskFactorsJson())
                ),
                new ScoredMetricResponse(
                        analytics.getAttentionScore(),
                        AttentionScoreCalculator.attentionLabel(analytics.getAttentionScore()),
                        analytics.getAttentionExplanation(),
                        factorSerializer.deserialize(analytics.getAttentionFactorsJson())
                ),
                analytics.getCompletionPercentage(),
                analytics.getExpectedProgress(),
                analytics.getProgressGap(),
                analytics.getOverdueRatio(),
                analytics.getAvgOverdueAgeDays(),
                analytics.getMaxOverdueAgeDays(),
                analytics.getScheduleVariance(),
                analytics.getBudgetVariance(),
                analytics.getCalculatedAt()
        );
    }

    private ProjectAnalyticsResponse toProjectAnalyticsResponse(
            ProjectEntity project,
            com.projectanalytics.analytics.domain.ProjectAnalyticsSnapshot snapshot
    ) {
        return new ProjectAnalyticsResponse(
                project.getId(),
                project.getName(),
                toMetricResponse(snapshot.health()),
                toMetricResponse(snapshot.risk()),
                toMetricResponse(snapshot.attention()),
                snapshot.completionPercentage(),
                snapshot.expectedProgress(),
                snapshot.progressGap(),
                snapshot.overdueRatio(),
                snapshot.avgOverdueAgeDays(),
                snapshot.maxOverdueAgeDays(),
                snapshot.scheduleVariance(),
                snapshot.budgetVariance(),
                snapshot.calculatedAt()
        );
    }

    private static ScoredMetricResponse toMetricResponse(ScoredMetric metric) {
        List<ScoreFactorResponse> factors = metric.factors().stream()
                .map(AnalyticsQueryService::toFactor)
                .toList();
        return new ScoredMetricResponse(metric.score(), metric.label(), metric.explanation(), factors);
    }

    private static ScoreFactorResponse toFactor(ScoreFactor factor) {
        return new ScoreFactorResponse(
                factor.code(),
                factor.description(),
                factor.contribution(),
                factor.rawValue()
        );
    }

    private static BigDecimal average(
            List<AnalyticsEntity> analytics,
            Function<AnalyticsEntity, BigDecimal> extractor
    ) {
        List<BigDecimal> values = analytics.stream().map(extractor).filter(v -> v != null).toList();
        if (values.isEmpty()) {
            return null;
        }
        BigDecimal sum = values.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        return sum.divide(BigDecimal.valueOf(values.size()), 2, RoundingMode.HALF_UP);
    }

    private boolean isProjectOverdue(ProjectEntity project) {
        if (project.getEndDate() == null) {
            return false;
        }
        if ("ARCHIVED".equalsIgnoreCase(nullSafe(project.getStatus()))) {
            return false;
        }
        return project.getEndDate().isBefore(LocalDate.now());
    }

    private static String buildExecutiveSummary(String scopeType, String name, ScopeAnalyticsKpiResponse kpis) {
        StringBuilder summary = new StringBuilder();
        summary.append(scopeType).append(" \"").append(name).append("\" covers ")
                .append(kpis.totalProjects())
                .append(" project(s) (").append(kpis.activeProjects()).append(" active). Average health is ")
                .append(formatScore(kpis.averageHealthScore())).append(", average risk ")
                .append(formatScore(kpis.averageRiskScore())).append(", and ")
                .append(kpis.highAttentionProjects()).append(" project(s) need elevated attention. ")
                .append(kpis.criticalProjects()).append(" project(s) are in critical health.");
        if (kpis.projectsBehindSchedule() > 0) {
            summary.append(' ').append(kpis.projectsBehindSchedule())
                    .append(" project(s) are behind schedule on progress gap (avg gap ")
                    .append(formatScore(kpis.averageProgressGap())).append(").");
        }
        return summary.toString();
    }

    private static List<String> buildInsights(ScopeAnalyticsKpiResponse kpis) {
        List<String> insights = new ArrayList<>();
        if (kpis.totalProjects() == 0) {
            insights.add("No synchronized projects are available in this scope yet.");
            return insights;
        }
        if (kpis.criticalProjects() > 0) {
            insights.add(kpis.criticalProjects() + " project(s) have critical health scores.");
        }
        if (kpis.highAttentionProjects() > 0) {
            insights.add(kpis.highAttentionProjects() + " project(s) have elevated attention scores.");
        }
        if (kpis.overdueProjects() > 0) {
            insights.add(kpis.overdueProjects() + " project(s) are past their planned end date.");
        }
        if (kpis.projectsBehindSchedule() > 0) {
            insights.add(kpis.projectsBehindSchedule()
                    + " project(s) trail expected schedule progress (negative progress gap). Average gap: "
                    + formatScore(kpis.averageProgressGap()) + ".");
        }
        if (kpis.projectsWithOverdueWorkPackages() > 0) {
            insights.add(kpis.projectsWithOverdueWorkPackages()
                    + " project(s) have overdue open work packages (avg overdue ratio "
                    + formatRatioPercent(kpis.averageOverdueRatio()) + ").");
        }
        if (kpis.averageCompletion() != null && kpis.averageExpectedProgress() != null) {
            insights.add("Portfolio progress: actual "
                    + formatScore(kpis.averageCompletion()) + "% vs expected "
                    + formatScore(kpis.averageExpectedProgress()) + "%.");
        }
        if (insights.isEmpty()) {
            insights.add("No critical operational risks detected from current analytics.");
        }
        return insights;
    }

    private static String formatRatioPercent(BigDecimal ratio) {
        if (ratio == null) {
            return "n/a";
        }
        return ratio.multiply(BigDecimal.valueOf(100)).setScale(0, RoundingMode.HALF_UP).toPlainString() + "%";
    }

    private static String formatScore(BigDecimal score) {
        return score == null ? "n/a" : score.toPlainString();
    }

    private ProjectEntity requireProject(UUID projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_001));
    }

    private static String nullSafe(String value) {
        return value == null ? "" : value;
    }
}
