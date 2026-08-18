package com.projectanalytics.dashboard;

import com.projectanalytics.analytics.application.AnalyticsRecalculationService;
import com.projectanalytics.dashboard.api.dto.ExecutiveDashboardResponse;
import com.projectanalytics.dashboard.application.ExecutiveDashboardService;
import com.projectanalytics.portfolio.persistence.PortfolioEntity;
import com.projectanalytics.portfolio.persistence.PortfolioRepository;
import com.projectanalytics.portfolio.persistence.PortfolioProjectEntity;
import com.projectanalytics.portfolio.persistence.PortfolioProjectRepository;
import com.projectanalytics.project.persistence.ProjectEntity;
import com.projectanalytics.project.persistence.ProjectRepository;
import com.projectanalytics.project.persistence.WorkPackageRepository;
import com.projectanalytics.analytics.persistence.AnalyticsRepository;
import com.projectanalytics.recommendation.persistence.RecommendationRepository;
import com.projectanalytics.analytics.persistence.AnalyticsSnapshotRepository;
import com.projectanalytics.synchronization.application.WorkspaceAccessService;
import com.projectanalytics.synchronization.persistence.SynchronizationHistoryRepository;
import com.projectanalytics.synchronization.persistence.WorkspaceEntity;
import com.projectanalytics.synchronization.persistence.WorkspaceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class ExecutiveDashboardServiceIntegrationTest {

    private static final UUID ADMIN_USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    @Autowired
    private RecommendationRepository recommendationRepository;

    @Autowired
    private WorkspaceAccessService workspaceAccessService;

    @Autowired
    private ExecutiveDashboardService executiveDashboardService;

    @Autowired
    private AnalyticsRecalculationService recalculationService;

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
    private AnalyticsRepository analyticsRepository;

    @Autowired
    private AnalyticsSnapshotRepository analyticsSnapshotRepository;

    @Autowired
    private SynchronizationHistoryRepository synchronizationHistoryRepository;

    @BeforeEach
    void setUp() {
        recommendationRepository.deleteAll();
        analyticsSnapshotRepository.deleteAll();
        analyticsRepository.deleteAll();
        workPackageRepository.deleteAll();
        portfolioProjectRepository.deleteAll();
        projectRepository.deleteAll();
        portfolioRepository.deleteAll();
        synchronizationHistoryRepository.deleteAll();
        workspaceRepository.deleteAll();

        WorkspaceEntity workspace = workspaceRepository.save(new WorkspaceEntity("Exec WS", "https://op-exec.test"));
        PortfolioEntity portfolio = portfolioRepository.save(new PortfolioEntity(workspace, "Default Portfolio", null));
        ProjectEntity project = new ProjectEntity(workspace, 1L, "Alpha");
        project.setStatus("ACTIVE");
        project.setProgress(new BigDecimal("50"));
        project.setStartDate(LocalDate.now().minusDays(20));
        project.setEndDate(LocalDate.now().plusDays(20));
        projectRepository.save(project);
        // Seed admin membership required for executive scoping.
        workspaceAccessService.grantConnectorAdmin(workspace.getId(), ADMIN_USER_ID);
        recalculationService.recalculateWorkspace(workspace.getId());
    }

    @Test
    void executiveDashboardComposesWorkspaceAnalyticsWithoutNewScoring() {
        ExecutiveDashboardResponse dashboard = executiveDashboardService.getExecutiveDashboard(ADMIN_USER_ID);

        assertThat(dashboard.workspaceCount()).isEqualTo(1);
        assertThat(dashboard.totalProjects()).isEqualTo(1);
        assertThat(dashboard.workspaces()).hasSize(1);
        assertThat(dashboard.workspaces().getFirst().workspaceName()).isEqualTo("Exec WS");
        assertThat(dashboard.topAttentionProjects()).isNotEmpty();
        assertThat(dashboard.insights()).isNotEmpty();
    }
}
