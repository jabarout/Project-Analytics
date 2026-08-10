package com.projectanalytics.reporting.api.dto;

import com.projectanalytics.reporting.domain.ReportFormat;
import com.projectanalytics.reporting.domain.ReportScopeType;
import com.projectanalytics.reporting.domain.ReportStatus;
import com.projectanalytics.reporting.domain.ReportType;

import java.time.Instant;
import java.util.UUID;

/**
 * Report history / status metadata (no file bytes).
 */
public record ReportResponse(
        UUID id,
        String title,
        ReportType reportType,
        ReportFormat format,
        ReportStatus status,
        ReportScopeType scopeType,
        UUID scopeId,
        UUID generatedBy,
        String fileName,
        String contentType,
        Long fileSizeBytes,
        String errorMessage,
        Instant generatedAt,
        Instant createdAt
) {
}
