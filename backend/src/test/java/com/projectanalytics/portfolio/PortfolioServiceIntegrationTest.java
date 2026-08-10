package com.projectanalytics.portfolio;

import com.projectanalytics.analytics.api.dto.ScopeAnalyticsKpiResponse;
import com.projectanalytics.analytics.api.dto.ScopeDashboardResponse;
import com.projectanalytics.common.exception.BusinessException;
import com.projectanalytics.common.exception.ErrorCode;
import com.projectanalytics.portfolio.api.dto.AssignProjectRequest;
import com.projectanalytics.portfolio.api.dto.CreatePortfolioRequest;
import com.projectanalytics.portfolio.api.dto.PortfolioDetailResponse;
import com.projectanalytics.portfolio.api.dto.PortfolioSummaryResponse;
import com.projectanalytics.portfolio.api.dto.UpdatePortfolioRequest;
import com.projectanalytics.portfolio.application.PortfolioService;
import com.projectanalytics.portfolio.persistence.PortfolioEntity;
import com.projectanalytics.portfolio.persistence.PortfolioProjectEntity;
import com.projectanalytics.portfolio.persistence.PortfolioProjectRepository;
import com.projectanalytics.portfolio.persistence.PortfolioRepository;
import com.projectanalytics.project.persistence.ProjectEntity;
import com.projectanalytics.project.persistence.ProjectRepository;
import com.projectanalytics.project.persistence.WorkPackageEntity;
import com.projectanalytics.project.persistence.WorkPackageRepository;
import com.projectanalytics.recommendation.persistence.RecommendationRepository;
import com.projectanalytics.synchronization.persistence.WorkspaceEntity;
import com.projectanalytics.synchronization.persistence.WorkspaceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Portfolio management uses local PostgreSQL data only (no OpenProject client).
 * Membership is many-to-many; workspace owns projects.
 */
@SpringBootTest
@ActiveProfiles("test")
class PortfolioServiceIntegrationTest {

    @Autowired
    private RecommendationRepository recommendationRepository;

    @Autowired
    private PortfolioService portfolioService;

    @Autowired
    private WorkspaceRepository workspaceRepository;

    @Autowired
    private PortfolioRepository portfolioRepository;

    @Autowired
    private PortfolioProjectRepository portfolioProjectRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private WorkPackageRepository workPackageRepository;

    @Autowired
    private com.projectanalytics.analytics.persistence.AnalyticsRepository analyticsRepository;

    @Autowired
    private com.projectanalytics.analytics.persistence.AnalyticsSnapshotRepository analyticsSnapshotRepository;

    private WorkspaceEntity workspace;
    private PortfolioEntity financePortfolio;
    private ProjectEntity activeProject;
    private ProjectEntity overdueProject;

    @BeforeEach
    void setUp() {
        recommendationRepository.deleteAll();
        analyticsSnapshotRepository.deleteAll();
        analyticsRepository.deleteAll();
        portfolioProjectRepository.deleteAll();
        workPackageRepository.deleteAll();
        projectRepository.deleteAll();
        portfolioRepository.deleteAll();
        workspaceRepository.deleteAll();

        workspace = workspaceRepository.save(new WorkspaceEntity("WS", "https://op.local.test"));
        financePortfolio = portfolioRepository.save(
                new PortfolioEntity(workspace, "Finance", "Finance view")
        );

        activeProject = new ProjectEntity(workspace, 1L, "Active Road");
        activeProject.setStatus("ACTIVE");
        activeProject.setBudget(new BigDecimal("1000.00"));
        activeProject.setProgress(new BigDecimal("40.00"));
        activeProject.setStartDate(LocalDate.now().minusMonths(1));
        activeProject.setEndDate(LocalDate.now().plusMonths(2));
        activeProject.setSynchronizedAt(Instant.parse("2026-07-01T10:00:00Z"));
        activeProject = projectRepository.save(activeProject);

        overdueProject = new ProjectEntity(workspace, 2L, "Late Bridge");
        overdueProject.setStatus("ACTIVE");
        overdueProject.setBudget(new BigDecimal("500.00"));
        overdueProject.setProgress(new BigDecimal("80.00"));
        overdueProject.setEndDate(LocalDate.now().minusDays(5));
        overdueProject.setSynchronizedAt(Instant.parse("2026-07-02T10:00:00Z"));
        overdueProject = projectRepository.save(overdueProject);

        portfolioProjectRepository.save(new PortfolioProjectEntity(financePortfolio.getId(), activeProject.getId()));
        portfolioProjectRepository.save(new PortfolioProjectEntity(financePortfolio.getId(), overdueProject.getId()));

        WorkPackageEntity openWp = new WorkPackageEntity(activeProject, 10L, "Task A");
        openWp.setStatus("In progress");
        openWp.setDueDate(LocalDate.now().minusDays(1));
        workPackageRepository.save(openWp);
    }

    @Test
    @DisplayName("list and detail read local portfolio memberships")
    void listAndDetail() {
        List<PortfolioSummaryResponse> list = portfolioService.listPortfolios(workspace.getId());
        assertThat(list).hasSize(1);
        assertThat(list.getFirst().totalProjects()).isEqualTo(2);
        assertThat(list.getFirst().activeProjects()).isEqualTo(2);

        PortfolioDetailResponse detail = portfolioService.getPortfolio(financePortfolio.getId());
        assertThat(detail.projects()).hasSize(2);
        assertThat(detail.workspaceId()).isEqualTo(workspace.getId());
    }

    @Test
    @DisplayName("KPIs and dashboard aggregate portfolio members only")
    void kpisAndDashboard() {
        ScopeAnalyticsKpiResponse kpis = portfolioService.getKpis(financePortfolio.getId());
        assertThat(kpis.totalProjects()).isEqualTo(2);
        assertThat(kpis.activeProjects()).isEqualTo(2);
        assertThat(kpis.overdueProjects()).isEqualTo(1);
        assertThat(kpis.totalWorkPackages()).isEqualTo(1);
        assertThat(kpis.totalBudget()).isEqualByComparingTo("1500.00");
        assertThat(kpis.averageHealthScore()).isNotNull();
        // Decision aggregates average stored ProgressMetrics fields (may be null pre-recalc until analytics exist)
        assertThat(kpis.projectsBehindSchedule()).isGreaterThanOrEqualTo(0);
        assertThat(kpis.projectsWithOverdueWorkPackages()).isGreaterThanOrEqualTo(0);

        ScopeDashboardResponse dashboard = portfolioService.getDashboard(financePortfolio.getId());
        assertThat(dashboard.scopeType()).isEqualTo("PORTFOLIO");
        assertThat(dashboard.executiveSummary()).contains("Finance");
        assertThat(dashboard.insights()).isNotEmpty();
        assertThat(dashboard.criticalHealthProjects()).isNotNull();
    }

    @Test
    @DisplayName("many-to-many add/remove membership and delete portfolio keep workspace projects")
    void membershipAndDelete() {
        PortfolioSummaryResponse employee = portfolioService.createPortfolio(
                new CreatePortfolioRequest(workspace.getId(), "Employee Projects", "People view")
        );

        PortfolioDetailResponse added = portfolioService.addProject(
                employee.id(),
                new AssignProjectRequest(activeProject.getId())
        );
        assertThat(added.totalProjects()).isEqualTo(1);

        // Still a member of Finance
        assertThat(portfolioService.getPortfolio(financePortfolio.getId()).totalProjects()).isEqualTo(2);
        // Workspace still owns both projects
        assertThat(projectRepository.findByWorkspaceIdOrderByNameAsc(workspace.getId())).hasSize(2);

        portfolioService.removeProject(employee.id(), activeProject.getId());
        assertThat(portfolioService.getPortfolio(employee.id()).totalProjects()).isEqualTo(0);

        portfolioService.addProject(employee.id(), new AssignProjectRequest(activeProject.getId()));
        portfolioService.deletePortfolio(employee.id());
        assertThat(portfolioRepository.findById(employee.id())).isEmpty();
        assertThat(projectRepository.findById(activeProject.getId())).isPresent();
    }

    @Test
    @DisplayName("duplicate portfolio name in workspace is rejected")
    void duplicateNameRejected() {
        assertThatThrownBy(() -> portfolioService.createPortfolio(
                new CreatePortfolioRequest(workspace.getId(), "Finance", null)
        ))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.PORTFOLIO_002);
    }

    @Test
    @DisplayName("create portfolio can bulk-add initial members")
    void createWithInitialMembers() {
        PortfolioSummaryResponse created = portfolioService.createPortfolio(
                new CreatePortfolioRequest(
                        workspace.getId(),
                        "Strategic",
                        "Initial bulk members",
                        List.of(activeProject.getId(), overdueProject.getId())
                )
        );
        PortfolioDetailResponse detail = portfolioService.getPortfolio(created.id());
        assertThat(detail.totalProjects()).isEqualTo(2);
        // Original Finance membership unchanged (many-to-many)
        assertThat(portfolioService.getPortfolio(financePortfolio.getId()).totalProjects()).isEqualTo(2);
    }
}
