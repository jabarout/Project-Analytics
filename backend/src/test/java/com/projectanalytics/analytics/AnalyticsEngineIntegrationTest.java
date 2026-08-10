package com.projectanalytics.analytics;

import com.projectanalytics.analytics.api.dto.ProjectAnalyticsResponse;
import com.projectanalytics.analytics.api.dto.ScopeDashboardResponse;
import com.projectanalytics.analytics.application.AnalyticsQueryService;
import com.projectanalytics.analytics.application.AnalyticsRecalculationService;
import com.projectanalytics.portfolio.persistence.PortfolioEntity;
import com.projectanalytics.portfolio.persistence.PortfolioProjectEntity;
import com.projectanalytics.portfolio.persistence.PortfolioProjectRepository;
import com.projectanalytics.portfolio.persistence.PortfolioRepository;
import com.projectanalytics.project.persistence.ProjectEntity;
import com.projectanalytics.project.persistence.ProjectRepository;
import com.projectanalytics.project.persistence.WorkPackageEntity;
import com.projectanalytics.project.persistence.WorkPackageRepository;
import com.projectanalytics.synchronization.persistence.WorkspaceEntity;
import com.projectanalytics.synchronization.persistence.WorkspaceRepository;
import com.projectanalytics.recommendation.persistence.RecommendationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class AnalyticsEngineIntegrationTest {

    @Autowired
    private RecommendationRepository recommendationRepository;


    @Autowired
    private AnalyticsRecalculationService recalculationService;

    @Autowired
    private AnalyticsQueryService analyticsQueryService;

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

    private WorkspaceEntity workspace;
    private PortfolioEntity portfolio;
    private ProjectEntity project;

    @Autowired
    private com.projectanalytics.analytics.persistence.AnalyticsRepository analyticsRepository;

    @Autowired
    private com.projectanalytics.analytics.persistence.AnalyticsSnapshotRepository snapshotRepository;

    @BeforeEach
    void cleanAndSeed() {
        recommendationRepository.deleteAll();
        snapshotRepository.deleteAll();
        analyticsRepository.deleteAll();
        workPackageRepository.deleteAll();
        portfolioProjectRepository.deleteAll();
        projectRepository.deleteAll();
        portfolioRepository.deleteAll();
        workspaceRepository.deleteAll();

        workspace = workspaceRepository.save(new WorkspaceEntity("Analytics WS", "https://op-analytics.test"));
        portfolio = portfolioRepository.save(
                new PortfolioEntity(workspace, "Core View", null)
        );
        project = new ProjectEntity(workspace, 1L, "Core Delivery");
        project.setStatus("ACTIVE");
        project.setProgress(new BigDecimal("55.00"));
        project.setStartDate(LocalDate.now().minusDays(30));
        project.setEndDate(LocalDate.now().plusDays(30));
        project = projectRepository.save(project);
        portfolioProjectRepository.save(new PortfolioProjectEntity(portfolio.getId(), project.getId()));

        WorkPackageEntity open = new WorkPackageEntity(project, 11L, "Open task");
        open.setStatus("New");
        open.setPriority("High");
        open.setDueDate(LocalDate.now().minusDays(2));
        workPackageRepository.save(open);

        WorkPackageEntity done = new WorkPackageEntity(project, 12L, "Done task");
        done.setStatus("Closed");
        workPackageRepository.save(done);
    }

    @Test
    @DisplayName("engine scores project and workspace dashboard reuses same analytics")
    void scoresProjectAndWorkspaceDashboard() {
        int count = recalculationService.recalculateWorkspace(workspace.getId());
        assertThat(count).isEqualTo(1);

        ProjectAnalyticsResponse analytics = analyticsQueryService.getProjectAnalytics(project.getId());
        assertThat(analytics.health().score()).isNotNull();
        assertThat(analytics.risk().score()).isNotNull();
        assertThat(analytics.attention().score()).isNotNull();
        assertThat(analytics.health().explanation()).isNotBlank();
        // WP completion 1/2 = 50%, not OP project.progress 55%
        assertThat(analytics.completionPercentage()).isEqualByComparingTo("50.00");
        assertThat(analytics.expectedProgress()).isNotNull();
        assertThat(analytics.progressGap()).isNotNull();
        assertThat(analytics.overdueRatio()).isEqualByComparingTo("0.5000");
        assertThat(analytics.avgOverdueAgeDays()).isEqualByComparingTo("2.00");
        assertThat(analytics.maxOverdueAgeDays()).isEqualTo(2);
        // endDate = today+30 → schedule variance -30 (days remaining)
        assertThat(analytics.scheduleVariance()).isEqualByComparingTo("-30.00");
        assertThat(analytics.health().factors()).isNotEmpty();
        assertThat(analytics.risk().factors()).isNotEmpty();

        ScopeDashboardResponse workspaceDashboard =
                analyticsQueryService.getWorkspaceDashboard(workspace.getId());
        assertThat(workspaceDashboard.scopeType()).isEqualTo("WORKSPACE");
        assertThat(workspaceDashboard.kpis().totalProjects()).isEqualTo(1);
        assertThat(workspaceDashboard.kpis().averageHealthScore()).isNotNull();
        assertThat(workspaceDashboard.topAttentionProjects()).isNotEmpty();

        ScopeDashboardResponse portfolioDashboard =
                analyticsQueryService.getPortfolioDashboard(portfolio.getId());
        assertThat(portfolioDashboard.scopeType()).isEqualTo("PORTFOLIO");
        assertThat(portfolioDashboard.kpis().averageHealthScore())
                .isEqualByComparingTo(workspaceDashboard.kpis().averageHealthScore());
    }
}
