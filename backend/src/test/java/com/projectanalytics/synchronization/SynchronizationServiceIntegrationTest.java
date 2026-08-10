package com.projectanalytics.synchronization;

import com.projectanalytics.infrastructure.openproject.OpenProjectClient;
import com.projectanalytics.infrastructure.openproject.OpenProjectConnectionProperties;
import com.projectanalytics.infrastructure.openproject.dto.OpenProjectProjectDto;
import com.projectanalytics.infrastructure.openproject.dto.OpenProjectWorkPackageDto;
import com.projectanalytics.portfolio.persistence.PortfolioRepository;
import com.projectanalytics.portfolio.persistence.PortfolioProjectEntity;
import com.projectanalytics.portfolio.persistence.PortfolioProjectRepository;
import com.projectanalytics.project.persistence.ProjectRepository;
import com.projectanalytics.project.persistence.WorkPackageRepository;
import com.projectanalytics.synchronization.application.SynchronizationResult;
import com.projectanalytics.synchronization.application.SynchronizationService;
import com.projectanalytics.synchronization.domain.SynchronizationStatus;
import com.projectanalytics.synchronization.domain.SynchronizationType;
import com.projectanalytics.synchronization.persistence.SynchronizationHistoryRepository;
import com.projectanalytics.synchronization.persistence.WorkspaceEntity;
import com.projectanalytics.synchronization.persistence.WorkspaceRepository;
import com.projectanalytics.recommendation.persistence.RecommendationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
class SynchronizationServiceIntegrationTest {

    @Autowired
    private RecommendationRepository recommendationRepository;


    @Autowired
    private SynchronizationService synchronizationService;

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
    private SynchronizationHistoryRepository historyRepository;

    @MockBean
    private OpenProjectClient openProjectClient;

    private WorkspaceEntity workspace;

    @Autowired
    private com.projectanalytics.analytics.persistence.AnalyticsRepository analyticsRepository;

    @Autowired
    private com.projectanalytics.analytics.persistence.AnalyticsSnapshotRepository analyticsSnapshotRepository;

    @BeforeEach
    void setUp() {
        recommendationRepository.deleteAll();
        analyticsSnapshotRepository.deleteAll();
        analyticsRepository.deleteAll();
        workPackageRepository.deleteAll();
        portfolioProjectRepository.deleteAll();
        projectRepository.deleteAll();
        portfolioRepository.deleteAll();
        historyRepository.deleteAll();
        workspaceRepository.deleteAll();

        workspace = workspaceRepository.save(new WorkspaceEntity("Demo", "https://openproject.example.test"));

        when(openProjectClient.fetchServerVersion(any(OpenProjectConnectionProperties.class)))
                .thenReturn("14.0.0");
        when(openProjectClient.fetchProjects(any(OpenProjectConnectionProperties.class), isNull()))
                .thenReturn(List.of(
                        new OpenProjectProjectDto(
                                10L,
                                "Bridge Replacement",
                                "Primary project",
                                "ACTIVE",
                                LocalDate.of(2026, 1, 1),
                                LocalDate.of(2026, 12, 31),
                                Instant.parse("2026-07-01T10:00:00Z"),
                                "Alice Admin"
                        )
                ));
        when(openProjectClient.fetchWorkPackages(any(OpenProjectConnectionProperties.class), eq(10L), isNull()))
                .thenReturn(List.of(
                        new OpenProjectWorkPackageDto(
                                100L,
                                10L,
                                "Design foundations",
                                "Task",
                                "In progress",
                                "High",
                                "Alice",
                                new BigDecimal("8.00"),
                                new BigDecimal("2.00"),
                                LocalDate.of(2026, 3, 15),
                                Instant.parse("2026-07-02T10:00:00Z")
                        )
                ));
    }

    @Test
    @DisplayName("manual synchronization imports projects and work packages")
    void synchronizeImportsOperationalData() {
        SynchronizationResult result = synchronizationService.synchronizeWorkspace(
                workspace.getId(),
                SynchronizationType.MANUAL
        );

        assertThat(result.status()).isEqualTo(SynchronizationStatus.SUCCESS);
        assertThat(result.syncType()).isEqualTo(SynchronizationType.INITIAL);
        assertThat(result.synchronizedProjects()).isEqualTo(1);
        assertThat(result.synchronizedWorkPackages()).isEqualTo(1);
        assertThat(projectRepository.count()).isEqualTo(1);
        assertThat(workPackageRepository.count()).isEqualTo(1);

        WorkspaceEntity reloaded = workspaceRepository.findById(workspace.getId()).orElseThrow();
        assertThat(reloaded.getSynchronizationStatus()).isEqualTo(SynchronizationStatus.SUCCESS);
        assertThat(reloaded.getVersion()).isEqualTo("14.0.0");
    }

    @Test
    @DisplayName("second concurrent-style run after success is incremental and updates existing rows")
    void secondRunIsIncremental() {
        synchronizationService.synchronizeWorkspace(workspace.getId(), SynchronizationType.MANUAL);

        when(openProjectClient.fetchProjects(any(OpenProjectConnectionProperties.class), any(Instant.class)))
                .thenReturn(List.of(
                        new OpenProjectProjectDto(
                                10L,
                                "Bridge Replacement Updated",
                                "Updated description",
                                "ACTIVE",
                                LocalDate.of(2026, 1, 1),
                                LocalDate.of(2026, 12, 31),
                                Instant.parse("2026-07-10T10:00:00Z"),
                                "Alice Admin"
                        )
                ));
        when(openProjectClient.fetchWorkPackages(any(OpenProjectConnectionProperties.class), eq(10L), any(Instant.class)))
                .thenReturn(List.of(
                        new OpenProjectWorkPackageDto(
                                100L,
                                10L,
                                "Design foundations revised",
                                "Task",
                                "Closed",
                                "High",
                                "Alice",
                                new BigDecimal("10.00"),
                                new BigDecimal("10.00"),
                                LocalDate.of(2026, 3, 20),
                                Instant.parse("2026-07-11T10:00:00Z")
                        )
                ));

        SynchronizationResult result = synchronizationService.synchronizeWorkspace(
                workspace.getId(),
                SynchronizationType.MANUAL
        );

        assertThat(result.status()).isEqualTo(SynchronizationStatus.SUCCESS);
        assertThat(result.syncType()).isEqualTo(SynchronizationType.MANUAL);
        assertThat(projectRepository.count()).isEqualTo(1);
        assertThat(projectRepository.findAll().getFirst().getName()).isEqualTo("Bridge Replacement Updated");
        assertThat(workPackageRepository.findAll().getFirst().getSubject()).isEqualTo("Design foundations revised");
    }

    @Test
    @DisplayName("OpenProject failure records FAILED history and does not leave partial success status")
    void failureIsAudited() {
        when(openProjectClient.fetchProjects(any(OpenProjectConnectionProperties.class), isNull()))
                .thenThrow(new com.projectanalytics.common.exception.BusinessException(
                        com.projectanalytics.common.exception.ErrorCode.SYNC_004
                ));

        assertThatThrownBy(() -> synchronizationService.synchronizeWorkspace(
                workspace.getId(),
                SynchronizationType.MANUAL
        )).isInstanceOf(com.projectanalytics.common.exception.BusinessException.class);

        WorkspaceEntity reloaded = workspaceRepository.findById(workspace.getId()).orElseThrow();
        assertThat(reloaded.getSynchronizationStatus()).isEqualTo(SynchronizationStatus.FAILED);
        assertThat(projectRepository.count()).isZero();

        SynchronizationResult status = synchronizationService.getLatestStatus(workspace.getId());
        assertThat(status.status()).isEqualTo(SynchronizationStatus.FAILED);
    }
}
