package com.projectanalytics.analytics.application;

import com.projectanalytics.analytics.domain.ProjectAnalyticsSnapshot;
import com.projectanalytics.analytics.domain.ProjectScoringInput;
import com.projectanalytics.analytics.persistence.AnalyticsEntity;
import com.projectanalytics.analytics.persistence.AnalyticsRepository;
import com.projectanalytics.analytics.persistence.AnalyticsSnapshotEntity;
import com.projectanalytics.analytics.persistence.AnalyticsSnapshotRepository;
import com.projectanalytics.analytics.scoring.ProjectAnalyticsEngine;
import com.projectanalytics.observability.PlatformMetrics;
import com.projectanalytics.portfolio.persistence.PortfolioEntity;
import com.projectanalytics.portfolio.persistence.PortfolioRepository;
import com.projectanalytics.project.persistence.ProjectEntity;
import com.projectanalytics.project.persistence.ProjectRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

/**
 * Recalculates and persists analytics for projects from local data only.
 */
@Service
public class AnalyticsRecalculationService {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsRecalculationService.class);

    private final ProjectRepository projectRepository;
    private final PortfolioRepository portfolioRepository;
    private final AnalyticsRepository analyticsRepository;
    private final AnalyticsSnapshotRepository snapshotRepository;
    private final ProjectScoringInputFactory inputFactory;
    private final ProjectAnalyticsEngine engine;
    private final ScoreFactorSerializer factorSerializer;
    private final PlatformMetrics platformMetrics;

    public AnalyticsRecalculationService(
            ProjectRepository projectRepository,
            PortfolioRepository portfolioRepository,
            AnalyticsRepository analyticsRepository,
            AnalyticsSnapshotRepository snapshotRepository,
            ProjectScoringInputFactory inputFactory,
            ProjectAnalyticsEngine engine,
            ScoreFactorSerializer factorSerializer,
            PlatformMetrics platformMetrics
    ) {
        this.projectRepository = projectRepository;
        this.portfolioRepository = portfolioRepository;
        this.analyticsRepository = analyticsRepository;
        this.snapshotRepository = snapshotRepository;
        this.inputFactory = inputFactory;
        this.engine = engine;
        this.factorSerializer = factorSerializer;
        this.platformMetrics = platformMetrics;
    }

    @Transactional
    public int recalculateWorkspace(UUID workspaceId) {
        long startedNanos = System.nanoTime();
        List<ProjectEntity> projects = projectRepository.findByWorkspaceIdOrderByNameAsc(workspaceId);
        int count = 0;
        for (ProjectEntity project : projects) {
            recalculateProject(project);
            count++;
        }
        refreshPortfolioStoredScores(workspaceId);
        long durationMs = (System.nanoTime() - startedNanos) / 1_000_000L;
        platformMetrics.recordAnalyticsRecalculation(durationMs, count);
        log.info(
                "Recalculated analytics for {} project(s) in workspace {} durationMs={}",
                count,
                workspaceId,
                durationMs
        );
        return count;
    }

    @Transactional
    public ProjectAnalyticsSnapshot recalculateProject(UUID projectId) {
        ProjectEntity project = projectRepository.findById(projectId)
                .orElseThrow(() -> new com.projectanalytics.common.exception.BusinessException(
                        com.projectanalytics.common.exception.ErrorCode.PROJECT_001
                ));
        return recalculateProject(project);
    }

    private ProjectAnalyticsSnapshot recalculateProject(ProjectEntity project) {
        ProjectScoringInput input = inputFactory.fromProject(project);
        ProjectAnalyticsSnapshot snapshot = engine.score(input);
        persist(project, snapshot);
        return snapshot;
    }

    private void persist(ProjectEntity project, ProjectAnalyticsSnapshot snapshot) {
        AnalyticsEntity entity = analyticsRepository.findByProjectId(project.getId())
                .orElseGet(() -> new AnalyticsEntity(project));
        entity.setHealthScore(snapshot.health().score());
        entity.setRiskScore(snapshot.risk().score());
        entity.setAttentionScore(snapshot.attention().score());
        entity.setCompletionPercentage(snapshot.completionPercentage());
        entity.setExpectedProgress(snapshot.expectedProgress());
        entity.setProgressGap(snapshot.progressGap());
        entity.setOverdueRatio(snapshot.overdueRatio());
        entity.setAvgOverdueAgeDays(snapshot.avgOverdueAgeDays());
        entity.setMaxOverdueAgeDays(snapshot.maxOverdueAgeDays());
        entity.setScheduleVariance(snapshot.scheduleVariance());
        entity.setBudgetVariance(snapshot.budgetVariance());
        entity.setHealthStatus(snapshot.health().label());
        entity.setHealthExplanation(snapshot.health().explanation());
        entity.setRiskLevel(snapshot.risk().label());
        entity.setRiskExplanation(snapshot.risk().explanation());
        entity.setAttentionExplanation(snapshot.attention().explanation());
        entity.setHealthFactorsJson(factorSerializer.serialize(snapshot.health().factors()));
        entity.setRiskFactorsJson(factorSerializer.serialize(snapshot.risk().factors()));
        entity.setAttentionFactorsJson(factorSerializer.serialize(snapshot.attention().factors()));
        entity.setCalculatedAt(snapshot.calculatedAt());
        analyticsRepository.save(entity);

        AnalyticsSnapshotEntity history = new AnalyticsSnapshotEntity(project);
        history.setHealthScore(snapshot.health().score());
        history.setRiskScore(snapshot.risk().score());
        history.setAttentionScore(snapshot.attention().score());
        history.setCompletionPercentage(snapshot.completionPercentage());
        history.setExpectedProgress(snapshot.expectedProgress());
        history.setProgressGap(snapshot.progressGap());
        history.setOverdueRatio(snapshot.overdueRatio());
        history.setCalculatedAt(snapshot.calculatedAt());
        snapshotRepository.save(history);
    }

    private void refreshPortfolioStoredScores(UUID workspaceId) {
        List<PortfolioEntity> portfolios = portfolioRepository.findByWorkspaceIdOrderByNameAsc(workspaceId);
        for (PortfolioEntity portfolio : portfolios) {
            BigDecimal avgHealth = analyticsRepository.averageHealthByPortfolioId(portfolio.getId());
            BigDecimal avgAttention = analyticsRepository.averageAttentionByPortfolioId(portfolio.getId());
            portfolio.setHealthScore(scale(avgHealth));
            portfolio.setAttentionScore(scale(avgAttention));
            portfolioRepository.save(portfolio);
        }
    }

    private static BigDecimal scale(BigDecimal value) {
        return value == null ? null : value.setScale(2, RoundingMode.HALF_UP);
    }
}
