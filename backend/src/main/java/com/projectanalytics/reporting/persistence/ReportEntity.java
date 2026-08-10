package com.projectanalytics.reporting.persistence;

import com.projectanalytics.common.persistence.BaseEntity;
import com.projectanalytics.reporting.domain.ReportFormat;
import com.projectanalytics.reporting.domain.ReportScopeType;
import com.projectanalytics.reporting.domain.ReportStatus;
import com.projectanalytics.reporting.domain.ReportType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * Persisted report history entry. File bytes live on disk; this row is metadata only.
 */
@Entity
@Table(name = "report")
public class ReportEntity extends BaseEntity {

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "report_type", nullable = false, length = 50)
    private ReportType reportType;

    @Enumerated(EnumType.STRING)
    @Column(name = "format", nullable = false, length = 20)
    private ReportFormat format;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private ReportStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "scope_type", length = 50)
    private ReportScopeType scopeType;

    @Column(name = "scope_id")
    private UUID scopeId;

    @Column(name = "generated_by", nullable = false)
    private UUID generatedBy;

    @Column(name = "file_path", length = 1000)
    private String filePath;

    @Column(name = "file_name", length = 255)
    private String fileName;

    @Column(name = "content_type", length = 100)
    private String contentType;

    @Column(name = "file_size_bytes")
    private Long fileSizeBytes;

    @Column(name = "error_message")
    private String errorMessage;

    @Column(name = "generated_at", nullable = false)
    private Instant generatedAt;

    protected ReportEntity() {
    }

    public ReportEntity(
            String title,
            ReportType reportType,
            ReportFormat format,
            ReportStatus status,
            ReportScopeType scopeType,
            UUID scopeId,
            UUID generatedBy,
            Instant generatedAt
    ) {
        this.title = title;
        this.reportType = reportType;
        this.format = format;
        this.status = status;
        this.scopeType = scopeType;
        this.scopeId = scopeId;
        this.generatedBy = generatedBy;
        this.generatedAt = generatedAt;
    }

    public String getTitle() {
        return title;
    }

    public ReportType getReportType() {
        return reportType;
    }

    public ReportFormat getFormat() {
        return format;
    }

    public ReportStatus getStatus() {
        return status;
    }

    public void setStatus(ReportStatus status) {
        this.status = status;
    }

    public ReportScopeType getScopeType() {
        return scopeType;
    }

    public UUID getScopeId() {
        return scopeId;
    }

    public UUID getGeneratedBy() {
        return generatedBy;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Long getFileSizeBytes() {
        return fileSizeBytes;
    }

    public void setFileSizeBytes(Long fileSizeBytes) {
        this.fileSizeBytes = fileSizeBytes;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Instant getGeneratedAt() {
        return generatedAt;
    }
}
