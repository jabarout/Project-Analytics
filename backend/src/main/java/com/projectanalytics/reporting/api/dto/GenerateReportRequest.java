package com.projectanalytics.reporting.api.dto;

import com.projectanalytics.reporting.domain.ReportFormat;
import com.projectanalytics.reporting.domain.ReportScopeType;
import com.projectanalytics.reporting.domain.ReportType;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Request to generate a formal PDF/Excel report from local analytics outputs.
 */
public record GenerateReportRequest(
        @NotNull ReportType reportType,
        @NotNull ReportFormat format,
        /**
         * Required for PORTFOLIO, PROJECT, KPI, and RISK reports. Not used for EXECUTIVE.
         */
        UUID scopeId,
        /**
         * Scope interpretation for KPI/RISK (WORKSPACE default when omitted).
         * For PORTFOLIO/PROJECT report types this is derived automatically.
         */
        ReportScopeType scopeType
) {
}
