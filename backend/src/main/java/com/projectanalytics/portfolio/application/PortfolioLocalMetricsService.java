package com.projectanalytics.portfolio.application;

import com.projectanalytics.portfolio.api.dto.PortfolioKpiResponse;
import com.projectanalytics.portfolio.api.dto.PortfolioProjectSummaryResponse;
import com.projectanalytics.portfolio.persistence.PortfolioEntity;
import com.projectanalytics.project.persistence.ProjectEntity;
import com.projectanalytics.project.persistence.ProjectRepository;
import com.projectanalytics.project.persistence.WorkPackageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * Aggregates portfolio metrics from local PostgreSQL only.
 * Must never call OpenProject or any external system.
 */
@Service
public class PortfolioLocalMetricsService {

    private static final String STATUS_ARCHIVED = "ARCHIVED";

    private final ProjectRepository projectRepository;
    private final WorkPackageRepository workPackageRepository;

    public PortfolioLocalMetricsService(
            ProjectRepository projectRepository,
            WorkPackageRepository workPackageRepository
    ) {
        this.projectRepository = projectRepository;
        this.workPackageRepository = workPackageRepository;
    }

    @Transactional(readOnly = true)
    public PortfolioKpiResponse computeKpis(PortfolioEntity portfolio) {
        UUID portfolioId = portfolio.getId();
        long totalProjects = projectRepository.countMembersByPortfolioId(portfolioId);
        long activeProjects = projectRepository.countActiveMembersByPortfolioId(portfolioId);
        long archivedProjects = projectRepository.countMembersByPortfolioIdAndStatusIgnoreCase(portfolioId, STATUS_ARCHIVED);
        long overdueProjects = projectRepository.countOverdueMembersByPortfolioId(portfolioId);
        long totalWorkPackages = workPackageRepository.countByPortfolioId(portfolioId);
        long overdueWorkPackages = workPackageRepository.countOverdueByPortfolioId(portfolioId);
        BigDecimal totalBudget = nullToZero(projectRepository.sumBudgetByPortfolioId(portfolioId));
        BigDecimal averageProgress = scale(projectRepository.averageProgressByPortfolioId(portfolioId));

        return new PortfolioKpiResponse(
                portfolioId,
                totalProjects,
                activeProjects,
                archivedProjects,
                overdueProjects,
                totalWorkPackages,
                overdueWorkPackages,
                totalBudget,
                averageProgress,
                portfolio.getHealthScore(),
                portfolio.getAttentionScore(),
                projectRepository.maxSynchronizedAtByPortfolioId(portfolioId)
        );
    }

    @Transactional(readOnly = true)
    public List<PortfolioProjectSummaryResponse> listProjectSummaries(UUID portfolioId) {
        return projectRepository.findMembersByPortfolioIdOrderByNameAsc(portfolioId).stream()
                .map(this::toProjectSummary)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PortfolioProjectSummaryResponse> listActiveProjects(UUID portfolioId) {
        return projectRepository.findMembersByPortfolioIdOrderByNameAsc(portfolioId).stream()
                .filter(ProjectEntity::isActiveLifecycle)
                .map(this::toProjectSummary)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PortfolioProjectSummaryResponse> listOverdueProjects(UUID portfolioId) {
        LocalDate today = LocalDate.now();
        return projectRepository.findMembersByPortfolioIdOrderByNameAsc(portfolioId).stream()
                .filter(project -> isOverdue(project, today))
                .sorted(Comparator.comparing(ProjectEntity::getEndDate, Comparator.nullsLast(Comparator.naturalOrder())))
                .map(this::toProjectSummary)
                .toList();
    }

    public String buildExecutiveSummary(String portfolioName, PortfolioKpiResponse kpis) {
        return "Portfolio \"" + portfolioName + "\" contains "
                + kpis.totalProjects() + " project(s) ("
                + kpis.activeProjects() + " active). "
                + kpis.overdueProjects() + " project(s) and "
                + kpis.overdueWorkPackages() + " work package(s) are past due based on local synchronized dates. "
                + "Average progress is "
                + (kpis.averageProgress() == null ? "n/a" : kpis.averageProgress() + "%")
                + ". Full Health/Risk/Attention scoring is provided by the Analytics Engine in a later milestone.";
    }

    public List<String> buildOperationalInsights(PortfolioKpiResponse kpis) {
        List<String> insights = new ArrayList<>();
        if (kpis.totalProjects() == 0) {
            insights.add("No synchronized projects are assigned to this portfolio yet.");
            return insights;
        }
        if (kpis.overdueProjects() > 0) {
            insights.add(kpis.overdueProjects() + " non-archived project(s) have an end date in the past.");
        }
        if (kpis.overdueWorkPackages() > 0) {
            insights.add(kpis.overdueWorkPackages() + " open work package(s) are past their due date.");
        }
        if (kpis.activeProjects() == 0 && kpis.totalProjects() > 0) {
            insights.add("All member projects are archived in the local synchronized copy.");
        }
        if (kpis.lastSynchronizedAt() == null) {
            insights.add("Projects have no synchronization timestamp; run OpenProject sync from Workspaces.");
        }
        if (insights.isEmpty()) {
            insights.add("No operational schedule risks detected from local synchronized dates.");
        }
        return insights;
    }

    public PortfolioProjectSummaryResponse toProjectSummary(ProjectEntity project) {
        return new PortfolioProjectSummaryResponse(
                project.getId(),
                project.getOpenProjectId(),
                project.getName(),
                project.getStatus(),
                project.getBudget(),
                project.getProgress(),
                project.getStartDate(),
                project.getEndDate(),
                project.getSynchronizedAt()
        );
    }

    private static boolean isOverdue(ProjectEntity project, LocalDate today) {
        if (project.getEndDate() == null) {
            return false;
        }
        if (STATUS_ARCHIVED.equalsIgnoreCase(nullToEmpty(project.getStatus()))) {
            return false;
        }
        return project.getEndDate().isBefore(today);
    }

    private static BigDecimal nullToZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP) : value.setScale(2, RoundingMode.HALF_UP);
    }

    private static BigDecimal scale(BigDecimal value) {
        return value == null ? null : value.setScale(2, RoundingMode.HALF_UP);
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
