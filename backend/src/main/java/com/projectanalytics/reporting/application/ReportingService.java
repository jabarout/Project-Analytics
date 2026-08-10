package com.projectanalytics.reporting.application;

import com.projectanalytics.common.exception.BusinessException;
import com.projectanalytics.common.exception.ErrorCode;
import com.projectanalytics.observability.PlatformMetrics;
import com.projectanalytics.reporting.api.dto.GenerateReportRequest;
import com.projectanalytics.reporting.api.dto.ReportResponse;
import com.projectanalytics.reporting.application.generator.ReportFileGenerator;
import com.projectanalytics.reporting.config.ReportingProperties;
import com.projectanalytics.reporting.domain.ReportFormat;
import com.projectanalytics.reporting.domain.ReportScopeType;
import com.projectanalytics.reporting.domain.ReportStatus;
import com.projectanalytics.reporting.domain.ReportType;
import com.projectanalytics.reporting.persistence.ReportEntity;
import com.projectanalytics.reporting.persistence.ReportRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Formal report generation workflow: assemble analytics view models → render PDF/Excel → persist history.
 * Does not introduce scoring formulas or OpenProject I/O.
 */
@Service
public class ReportingService {

    private static final Logger log = LoggerFactory.getLogger(ReportingService.class);

    private final ReportRepository reportRepository;
    private final ReportContentAssembler contentAssembler;
    private final ReportingProperties reportingProperties;
    private final Map<ReportFormat, ReportFileGenerator> generators;
    private final PlatformMetrics platformMetrics;

    public ReportingService(
            ReportRepository reportRepository,
            ReportContentAssembler contentAssembler,
            ReportingProperties reportingProperties,
            List<ReportFileGenerator> generatorList,
            PlatformMetrics platformMetrics
    ) {
        this.reportRepository = reportRepository;
        this.contentAssembler = contentAssembler;
        this.reportingProperties = reportingProperties;
        this.platformMetrics = platformMetrics;
        Map<ReportFormat, ReportFileGenerator> map = new EnumMap<>(ReportFormat.class);
        for (ReportFileGenerator generator : generatorList) {
            map.put(generator.format(), generator);
        }
        this.generators = Map.copyOf(map);
    }

    @Transactional
    public ReportResponse generate(GenerateReportRequest request, UUID generatedBy) {
        ReportScopeType effectiveScopeType = resolveScopeType(request);
        Instant generatedAt = Instant.now();
        long startedNanos = System.nanoTime();

        ReportDocument document;
        try {
            document = contentAssembler.assemble(request.reportType(), effectiveScopeType, request.scopeId());
        } catch (BusinessException exception) {
            platformMetrics.recordReportGenerated(
                    request.reportType().name(),
                    request.format().name(),
                    "failed",
                    (System.nanoTime() - startedNanos) / 1_000_000L
            );
            throw exception;
        } catch (Exception exception) {
            platformMetrics.recordReportGenerated(
                    request.reportType().name(),
                    request.format().name(),
                    "failed",
                    (System.nanoTime() - startedNanos) / 1_000_000L
            );
            throw new BusinessException(ErrorCode.REPORT_002, "Report generation failed.", exception);
        }

        ReportEntity entity = new ReportEntity(
                document.title(),
                request.reportType(),
                request.format(),
                ReportStatus.COMPLETED,
                effectiveScopeType,
                request.scopeId(),
                generatedBy,
                generatedAt
        );

        try {
            ReportFileGenerator generator = generators.get(request.format());
            if (generator == null) {
                throw new BusinessException(ErrorCode.VALIDATION_005, "Unsupported report format: " + request.format());
            }
            byte[] bytes = generator.generate(document);
            if (bytes.length > reportingProperties.getMaxSizeBytes()) {
                throw new BusinessException(
                        ErrorCode.REPORT_004,
                        "Generated report exceeds configured maximum size ("
                                + reportingProperties.getMaxSizeBytes() + " bytes)."
                );
            }

            Path storageRoot = Path.of(reportingProperties.getStoragePath()).toAbsolutePath().normalize();
            Files.createDirectories(storageRoot);

            String safeTitle = sanitizeFileName(document.title());
            String fileName = safeTitle + "-" + generatedAt.toEpochMilli() + "." + request.format().getFileExtension();
            Path filePath = storageRoot.resolve(fileName);
            Files.write(filePath, bytes);

            entity.setFilePath(filePath.toString());
            entity.setFileName(fileName);
            entity.setContentType(request.format().getContentType());
            entity.setFileSizeBytes((long) bytes.length);
            entity.setStatus(ReportStatus.COMPLETED);
            long durationMs = (System.nanoTime() - startedNanos) / 1_000_000L;
            platformMetrics.recordReportGenerated(
                    request.reportType().name(),
                    request.format().name(),
                    "completed",
                    durationMs
            );
            log.info(
                    "Generated report type={} format={} path={} durationMs={}",
                    request.reportType(),
                    request.format(),
                    filePath,
                    durationMs
            );
        } catch (BusinessException exception) {
            entity.setStatus(ReportStatus.FAILED);
            entity.setErrorMessage(exception.getMessage());
            reportRepository.save(entity);
            platformMetrics.recordReportGenerated(
                    request.reportType().name(),
                    request.format().name(),
                    "failed",
                    (System.nanoTime() - startedNanos) / 1_000_000L
            );
            throw exception;
        } catch (IOException exception) {
            entity.setStatus(ReportStatus.FAILED);
            entity.setErrorMessage(exception.getMessage());
            reportRepository.save(entity);
            platformMetrics.recordReportGenerated(
                    request.reportType().name(),
                    request.format().name(),
                    "failed",
                    (System.nanoTime() - startedNanos) / 1_000_000L
            );
            throw new BusinessException(ErrorCode.REPORT_004, "Report export failed.", exception);
        } catch (Exception exception) {
            entity.setStatus(ReportStatus.FAILED);
            entity.setErrorMessage(exception.getMessage());
            reportRepository.save(entity);
            platformMetrics.recordReportGenerated(
                    request.reportType().name(),
                    request.format().name(),
                    "failed",
                    (System.nanoTime() - startedNanos) / 1_000_000L
            );
            throw new BusinessException(ErrorCode.REPORT_002, "Report generation failed.", exception);
        }

        return toResponse(reportRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public List<ReportResponse> listHistory() {
        return reportRepository.findAllByOrderByGeneratedAtDesc().stream()
                .map(ReportingService::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ReportResponse getReport(UUID id) {
        return toResponse(requireReport(id));
    }

    @Transactional(readOnly = true)
    public ReportFileDownload download(UUID id) {
        ReportEntity entity = requireReport(id);
        if (entity.getStatus() != ReportStatus.COMPLETED
                || entity.getFilePath() == null
                || entity.getFilePath().isBlank()) {
            throw new BusinessException(ErrorCode.REPORT_004, "Report file is not available for download.");
        }
        Path path = Path.of(entity.getFilePath());
        if (!Files.isRegularFile(path)) {
            throw new BusinessException(ErrorCode.REPORT_004, "Report file is missing from storage.");
        }
        try {
            byte[] bytes = Files.readAllBytes(path);
            String fileName = entity.getFileName() != null ? entity.getFileName() : path.getFileName().toString();
            String contentType = entity.getContentType() != null
                    ? entity.getContentType()
                    : entity.getFormat().getContentType();
            return new ReportFileDownload(fileName, contentType, bytes);
        } catch (IOException exception) {
            throw new BusinessException(ErrorCode.REPORT_004, "Report export failed.", exception);
        }
    }

    private ReportEntity requireReport(UUID id) {
        return reportRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.REPORT_001));
    }

    private static ReportScopeType resolveScopeType(GenerateReportRequest request) {
        return switch (request.reportType()) {
            case EXECUTIVE -> null;
            case PORTFOLIO -> ReportScopeType.PORTFOLIO;
            case PROJECT -> ReportScopeType.PROJECT;
            case KPI, RISK -> request.scopeType() != null ? request.scopeType() : ReportScopeType.WORKSPACE;
        };
    }

    private static String sanitizeFileName(String title) {
        String sanitized = title == null ? "report" : title.replaceAll("[^a-zA-Z0-9._-]+", "_");
        if (sanitized.length() > 80) {
            sanitized = sanitized.substring(0, 80);
        }
        return sanitized.isBlank() ? "report" : sanitized;
    }

    private static ReportResponse toResponse(ReportEntity entity) {
        return new ReportResponse(
                entity.getId(),
                entity.getTitle(),
                entity.getReportType(),
                entity.getFormat(),
                entity.getStatus(),
                entity.getScopeType(),
                entity.getScopeId(),
                entity.getGeneratedBy(),
                entity.getFileName(),
                entity.getContentType(),
                entity.getFileSizeBytes(),
                entity.getErrorMessage(),
                entity.getGeneratedAt(),
                entity.getCreatedAt()
        );
    }

    public record ReportFileDownload(String fileName, String contentType, byte[] content) {
    }
}
