package com.projectanalytics.analytics;

import com.projectanalytics.analytics.application.AnalyticsSnapshotRetentionService;
import com.projectanalytics.analytics.configuration.AnalyticsScoringProperties;
import com.projectanalytics.analytics.persistence.AnalyticsSnapshotEntity;
import com.projectanalytics.analytics.persistence.AnalyticsSnapshotRepository;
import com.projectanalytics.project.persistence.ProjectEntity;
import com.projectanalytics.project.persistence.ProjectRepository;
import com.projectanalytics.synchronization.persistence.WorkspaceEntity;
import com.projectanalytics.synchronization.persistence.WorkspaceRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class AnalyticsSnapshotRetentionServiceTest {

    @Autowired
    private AnalyticsSnapshotRetentionService retentionService;

    @Autowired
    private AnalyticsSnapshotRepository snapshotRepository;

    @Autowired
    private AnalyticsScoringProperties analyticsProperties;

    @Autowired
    private WorkspaceRepository workspaceRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Test
    void purgeRemovesSnapshotsOlderThanRetention() {
        analyticsProperties.setSnapshotRetentionDays(7);
        analyticsProperties.setSnapshotPurgeEnabled(true);

        WorkspaceEntity workspace = workspaceRepository.save(
                new WorkspaceEntity("Retention WS", "https://openproject.retention." + UUID.randomUUID() + ".test")
        );
        ProjectEntity project = projectRepository.save(
                new ProjectEntity(workspace, System.nanoTime(), "Retention Project")
        );

        AnalyticsSnapshotEntity oldSnap = new AnalyticsSnapshotEntity(project);
        oldSnap.setHealthScore(BigDecimal.valueOf(50));
        oldSnap.setRiskScore(BigDecimal.valueOf(40));
        oldSnap.setAttentionScore(BigDecimal.valueOf(30));
        oldSnap.setCompletionPercentage(BigDecimal.valueOf(20));
        oldSnap.setCalculatedAt(Instant.now().minus(40, ChronoUnit.DAYS));
        UUID oldId = snapshotRepository.save(oldSnap).getId();

        AnalyticsSnapshotEntity recentSnap = new AnalyticsSnapshotEntity(project);
        recentSnap.setHealthScore(BigDecimal.valueOf(70));
        recentSnap.setRiskScore(BigDecimal.valueOf(20));
        recentSnap.setAttentionScore(BigDecimal.valueOf(10));
        recentSnap.setCompletionPercentage(BigDecimal.valueOf(60));
        recentSnap.setCalculatedAt(Instant.now().minus(1, ChronoUnit.DAYS));
        UUID recentId = snapshotRepository.save(recentSnap).getId();

        retentionService.purgeExpiredSnapshots();

        List<AnalyticsSnapshotEntity> remainingForProject =
                snapshotRepository.findTop20ByProjectIdOrderByCalculatedAtDesc(project.getId());
        assertThat(remainingForProject).extracting(AnalyticsSnapshotEntity::getId).containsExactly(recentId);
        assertThat(snapshotRepository.findById(oldId)).isEmpty();
        assertThat(remainingForProject.getFirst().getHealthScore())
                .isEqualByComparingTo(BigDecimal.valueOf(70));
    }
}
