package com.projectanalytics.recommendation;

import com.projectanalytics.analytics.application.AnalyticsRecalculationService;
import com.projectanalytics.analytics.persistence.AnalyticsRepository;
import com.projectanalytics.analytics.persistence.AnalyticsSnapshotEntity;
import com.projectanalytics.analytics.persistence.AnalyticsSnapshotRepository;
import com.projectanalytics.portfolio.persistence.PortfolioEntity;
import com.projectanalytics.portfolio.persistence.PortfolioRepository;
import com.projectanalytics.portfolio.persistence.PortfolioProjectEntity;
import com.projectanalytics.portfolio.persistence.PortfolioProjectRepository;
import com.projectanalytics.project.persistence.ProjectEntity;
import com.projectanalytics.project.persistence.ProjectRepository;
import com.projectanalytics.project.persistence.WorkPackageRepository;
import com.projectanalytics.recommendation.api.dto.RecommendationBundleResponse;
import com.projectanalytics.recommendation.api.dto.RecommendationResponse;
import com.projectanalytics.recommendation.application.RecommendationService;
import com.projectanalytics.recommendation.domain.RecommendationSeverity;
import com.projectanalytics.recommendation.persistence.RecommendationRepository;
import com.projectanalytics.synchronization.persistence.WorkspaceEntity;
import com.projectanalytics.synchronization.persistence.WorkspaceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
class RecommendationServiceIntegrationTest {

    @Autowired
    private RecommendationService recommendationService;

    @Autowired
    private RecommendationRepository recommendationRepository;

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

    private UUID workspaceId;
    private UUID portfolioId;
    private UUID projectId;

    @BeforeEach
    void setUp() {
        recommendationRepository.deleteAll();
        analyticsSnapshotRepository.deleteAll();
        analyticsRepository.deleteAll();
        workPackageRepository.deleteAll();
        portfolioProjectRepository.deleteAll();
        projectRepository.deleteAll();
        portfolioRepository.deleteAll();
        workspaceRepository.deleteAll();

        WorkspaceEntity workspace = workspaceRepository.save(new WorkspaceEntity("Reco WS", "https://op-reco.test"));
        PortfolioEntity portfolio = portfolioRepository.save(new PortfolioEntity(workspace, "Default Portfolio", null));
        ProjectEntity project = new ProjectEntity(workspace, 99L, "At Risk Project");
        project.setStatus("ACTIVE");
        project.setProgress(new BigDecimal("20"));
        project.setStartDate(LocalDate.now().minusDays(90));
        project.setEndDate(LocalDate.now().minusDays(5)); // overdue → lower health / higher risk & attention
        project = projectRepository.save(project);
        portfolioProjectRepository.save(new PortfolioProjectEntity(portfolio.getId(), project.getId()));

        workspaceId = workspace.getId();
        portfolioId = portfolio.getId();
        projectId = project.getId();
        recalculationService.recalculateWorkspace(workspaceId);
    }

    @Test
    void generatesExplainableProjectRecommendationsFromAnalyticsOnly() {
        RecommendationBundleResponse bundle = recommendationService.getProjectRecommendations(projectId);

        assertThat(bundle.scopeType()).isEqualTo("PROJECT");
        assertThat(bundle.scopeName()).isEqualTo("At Risk Project");
        assertThat(bundle.recommendations()).isNotEmpty();
        assertThat(bundle.executiveSummary()).isNotBlank();

        RecommendationResponse first = bundle.recommendations().getFirst();
        assertThat(first.ruleCode()).isNotBlank();
        assertThat(first.title()).isNotBlank();
        assertThat(first.explanation()).isNotBlank();
        assertThat(first.suggestedAction()).isNotBlank();
        assertThat(first.supportingMetrics()).isNotEmpty();
        assertThat(first.severity()).isIn(
                RecommendationSeverity.CRITICAL,
                RecommendationSeverity.HIGH,
                RecommendationSeverity.MEDIUM,
                RecommendationSeverity.LOW
        );

        RecommendationResponse fetched = recommendationService.getRecommendation(first.id());
        assertThat(fetched.id()).isEqualTo(first.id());
        assertThat(fetched.projectId()).isEqualTo(projectId);
    }

    @Test
    void workspaceAndExecutiveBundlesPrioritizeRecommendations() {
        // Seed an older healthier snapshot so declining-health rule can fire after recalc snapshot exists.
        analyticsRepository.findByProjectId(projectId).ifPresent(analytics -> {
            AnalyticsSnapshotEntity older = new AnalyticsSnapshotEntity(analytics.getProject());
            older.setHealthScore(new BigDecimal("85.00"));
            older.setRiskScore(new BigDecimal("20.00"));
            older.setAttentionScore(new BigDecimal("15.00"));
            older.setCompletionPercentage(new BigDecimal("80.00"));
            older.setCalculatedAt(Instant.now().minusSeconds(86_400));
            analyticsSnapshotRepository.save(older);
        });

        RecommendationBundleResponse workspace = recommendationService.getWorkspaceRecommendations(workspaceId);
        assertThat(workspace.scopeType()).isEqualTo("WORKSPACE");
        assertThat(workspace.recommendations()).isNotEmpty();

        RecommendationBundleResponse portfolio = recommendationService.getPortfolioRecommendations(portfolioId);
        assertThat(portfolio.scopeType()).isEqualTo("PORTFOLIO");
        assertThat(portfolio.recommendations()).isNotEmpty();

        RecommendationBundleResponse executive = recommendationService.getExecutiveRecommendations();
        assertThat(executive.scopeType()).isEqualTo("EXECUTIVE");
        assertThat(executive.recommendations()).isNotEmpty();
        assertThat(executive.recommendations().getFirst().priorityRank()).isGreaterThan(0);
    }

    @Test
    void missingRecommendationReturnsCataloguedError() {
        assertThatThrownBy(() -> recommendationService.getRecommendation(UUID.randomUUID()))
                .hasMessageContaining("Recommendation not found");
    }
}
