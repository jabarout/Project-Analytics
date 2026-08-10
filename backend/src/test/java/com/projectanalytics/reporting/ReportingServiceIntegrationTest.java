package com.projectanalytics.reporting;

import com.projectanalytics.analytics.application.AnalyticsRecalculationService;
import com.projectanalytics.analytics.persistence.AnalyticsRepository;
import com.projectanalytics.recommendation.persistence.RecommendationRepository;
import com.projectanalytics.analytics.persistence.AnalyticsSnapshotRepository;
import com.projectanalytics.portfolio.persistence.PortfolioEntity;
import com.projectanalytics.portfolio.persistence.PortfolioRepository;
import com.projectanalytics.portfolio.persistence.PortfolioProjectEntity;
import com.projectanalytics.portfolio.persistence.PortfolioProjectRepository;
import com.projectanalytics.project.persistence.ProjectEntity;
import com.projectanalytics.project.persistence.ProjectRepository;
import com.projectanalytics.project.persistence.WorkPackageRepository;
import com.projectanalytics.reporting.api.dto.GenerateReportRequest;
import com.projectanalytics.reporting.api.dto.ReportResponse;
import com.projectanalytics.reporting.application.ReportingService;
import com.projectanalytics.reporting.application.ReportingService.ReportFileDownload;
import com.projectanalytics.reporting.domain.ReportFormat;
import com.projectanalytics.reporting.domain.ReportScopeType;
import com.projectanalytics.reporting.domain.ReportStatus;
import com.projectanalytics.reporting.domain.ReportType;
import com.projectanalytics.reporting.persistence.ReportRepository;
import com.projectanalytics.synchronization.persistence.WorkspaceEntity;
import com.projectanalytics.synchronization.persistence.WorkspaceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
class ReportingServiceIntegrationTest {

    @Autowired
    private RecommendationRepository recommendationRepository;


    private static final UUID ADMIN_USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    @Autowired
    private ReportingService reportingService;

    @Autowired
    private ReportRepository reportRepository;

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
    void setUp() throws Exception {
        reportRepository.deleteAll();
        recommendationRepository.deleteAll();
        analyticsSnapshotRepository.deleteAll();
        analyticsRepository.deleteAll();
        workPackageRepository.deleteAll();
        portfolioProjectRepository.deleteAll();
        projectRepository.deleteAll();
        portfolioRepository.deleteAll();
        workspaceRepository.deleteAll();

        WorkspaceEntity workspace = workspaceRepository.save(new WorkspaceEntity("Report WS", "https://op-report.test"));
        PortfolioEntity portfolio = portfolioRepository.save(new PortfolioEntity(workspace, "Default Portfolio", null));
        ProjectEntity project = new ProjectEntity(workspace, 42L, "Report Project");
        project.setStatus("ACTIVE");
        project.setProgress(new BigDecimal("40"));
        project.setStartDate(LocalDate.now().minusDays(30));
        project.setEndDate(LocalDate.now().plusDays(30));
        project = projectRepository.save(project);

        workspaceId = workspace.getId();
        portfolioId = portfolio.getId();
        projectId = project.getId();
        recalculationService.recalculateWorkspace(workspaceId);
    }

    @Test
    void generatesExecutivePdfAndSupportsHistoryAndDownload() {
        ReportResponse report = reportingService.generate(
                new GenerateReportRequest(ReportType.EXECUTIVE, ReportFormat.PDF, null, null),
                ADMIN_USER_ID
        );

        assertThat(report.status()).isEqualTo(ReportStatus.COMPLETED);
        assertThat(report.reportType()).isEqualTo(ReportType.EXECUTIVE);
        assertThat(report.format()).isEqualTo(ReportFormat.PDF);
        assertThat(report.fileName()).endsWith(".pdf");
        assertThat(report.fileSizeBytes()).isPositive();
        assertThat(Files.isRegularFile(Path.of(
                reportRepository.findById(report.id()).orElseThrow().getFilePath()
        ))).isTrue();

        List<ReportResponse> history = reportingService.listHistory();
        assertThat(history).extracting(ReportResponse::id).contains(report.id());

        ReportResponse fetched = reportingService.getReport(report.id());
        assertThat(fetched.title()).contains("Executive");

        ReportFileDownload download = reportingService.download(report.id());
        assertThat(download.contentType()).isEqualTo("application/pdf");
        assertThat(download.content().length).isGreaterThan(100);
        assertThat(download.content()[0]).isEqualTo((byte) '%');
        assertThat(download.content()[1]).isEqualTo((byte) 'P');
        assertThat(download.content()[2]).isEqualTo((byte) 'D');
        assertThat(download.content()[3]).isEqualTo((byte) 'F');
    }

    @Test
    void generatesWorkspaceKpiExcelAndPortfolioRiskPdf() {
        ReportResponse kpiExcel = reportingService.generate(
                new GenerateReportRequest(ReportType.KPI, ReportFormat.EXCEL, workspaceId, ReportScopeType.WORKSPACE),
                ADMIN_USER_ID
        );
        assertThat(kpiExcel.status()).isEqualTo(ReportStatus.COMPLETED);
        assertThat(kpiExcel.fileName()).endsWith(".xlsx");

        ReportFileDownload excel = reportingService.download(kpiExcel.id());
        assertThat(excel.contentType()).contains("spreadsheet");
        assertThat(excel.content().length).isGreaterThan(50);
        // XLSX is a zip: PK header
        assertThat(excel.content()[0]).isEqualTo((byte) 'P');
        assertThat(excel.content()[1]).isEqualTo((byte) 'K');

        ReportResponse riskPdf = reportingService.generate(
                new GenerateReportRequest(ReportType.RISK, ReportFormat.PDF, portfolioId, ReportScopeType.PORTFOLIO),
                ADMIN_USER_ID
        );
        assertThat(riskPdf.status()).isEqualTo(ReportStatus.COMPLETED);
        assertThat(riskPdf.scopeType()).isEqualTo(ReportScopeType.PORTFOLIO);

        ReportResponse projectReport = reportingService.generate(
                new GenerateReportRequest(ReportType.PROJECT, ReportFormat.PDF, projectId, null),
                ADMIN_USER_ID
        );
        assertThat(projectReport.status()).isEqualTo(ReportStatus.COMPLETED);
        assertThat(projectReport.title()).contains("Report Project");
    }

    @Test
    void rejectsMissingScopeForPortfolioReport() {
        assertThatThrownBy(() -> reportingService.generate(
                new GenerateReportRequest(ReportType.PORTFOLIO, ReportFormat.PDF, null, null),
                ADMIN_USER_ID
        )).hasMessageContaining("scopeId");
    }

    @Test
    void unknownReportReturnsNotFound() {
        assertThatThrownBy(() -> reportingService.getReport(UUID.randomUUID()))
                .hasMessageContaining("Report not found");
    }
}
