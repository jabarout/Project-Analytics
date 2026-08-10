package com.projectanalytics.reporting;

import com.projectanalytics.observability.PlatformMetrics;
import com.projectanalytics.reporting.application.ReportRetentionService;
import com.projectanalytics.reporting.config.ReportingProperties;
import com.projectanalytics.reporting.domain.ReportFormat;
import com.projectanalytics.reporting.domain.ReportScopeType;
import com.projectanalytics.reporting.domain.ReportStatus;
import com.projectanalytics.reporting.domain.ReportType;
import com.projectanalytics.reporting.persistence.ReportEntity;
import com.projectanalytics.reporting.persistence.ReportRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class ReportRetentionServiceTest {

    private static final UUID USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    @TempDir
    Path tempDir;

    @Autowired
    private ReportRetentionService reportRetentionService;

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private ReportingProperties reportingProperties;

    @Autowired
    private PlatformMetrics platformMetrics;

    @BeforeEach
    void cleanReports() {
        reportRepository.deleteAll();
        reportingProperties.setStoragePath(tempDir.toString());
        reportingProperties.setRetentionDays(7);
        reportingProperties.setPurgeEnabled(true);
    }

    @Test
    void purgeRemovesOldReportsAndFiles() throws Exception {
        Path oldFile = tempDir.resolve("old-report.pdf");
        Files.writeString(oldFile, "old");
        Path newFile = tempDir.resolve("new-report.pdf");
        Files.writeString(newFile, "new");

        ReportEntity oldReport = new ReportEntity(
                "Old",
                ReportType.EXECUTIVE,
                ReportFormat.PDF,
                ReportStatus.COMPLETED,
                ReportScopeType.WORKSPACE,
                UUID.randomUUID(),
                USER_ID,
                Instant.now().minus(30, ChronoUnit.DAYS)
        );
        oldReport.setFilePath(oldFile.toAbsolutePath().toString());
        oldReport.setFileName("old-report.pdf");
        reportRepository.save(oldReport);

        ReportEntity newReport = new ReportEntity(
                "New",
                ReportType.EXECUTIVE,
                ReportFormat.PDF,
                ReportStatus.COMPLETED,
                ReportScopeType.WORKSPACE,
                UUID.randomUUID(),
                USER_ID,
                Instant.now().minus(1, ChronoUnit.DAYS)
        );
        newReport.setFilePath(newFile.toAbsolutePath().toString());
        newReport.setFileName("new-report.pdf");
        reportRepository.save(newReport);

        reportRetentionService.purgeExpiredReports();

        assertThat(reportRepository.findAll()).hasSize(1);
        assertThat(reportRepository.findAll().getFirst().getTitle()).isEqualTo("New");
        assertThat(Files.exists(oldFile)).isFalse();
        assertThat(Files.exists(newFile)).isTrue();
        assertThat(platformMetrics).isNotNull();
    }
}
