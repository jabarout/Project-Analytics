package com.projectanalytics.reporting.api;

import com.projectanalytics.authentication.security.AuthenticatedUser;
import com.projectanalytics.common.api.ApiResponse;
import com.projectanalytics.common.constants.ApplicationConstants;
import com.projectanalytics.reporting.api.dto.GenerateReportRequest;
import com.projectanalytics.reporting.api.dto.ReportResponse;
import com.projectanalytics.reporting.application.ReportingService;
import com.projectanalytics.reporting.application.ReportingService.ReportFileDownload;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Formal reporting endpoints (API Specification §13).
 * Controllers remain thin: validate, delegate, return DTOs or file bytes.
 */
@RestController
@RequestMapping(ApplicationConstants.API_V1_BASE_PATH + "/reports")
@Tag(name = "Reports", description = "PDF/Excel report generation, history, and download")
@SecurityRequirement(name = "bearerAuth")
public class ReportController {

    private final ReportingService reportingService;

    public ReportController(ReportingService reportingService) {
        this.reportingService = reportingService;
    }

    @PostMapping
    @Operation(summary = "Generate report", description = "Builds a PDF or Excel report from local analytics/dashboard outputs.")
    public ApiResponse<ReportResponse> generate(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody GenerateReportRequest request
    ) {
        return ApiResponse.of(reportingService.generate(request, user.getId()));
    }

    @GetMapping
    @Operation(summary = "Report history", description = "Lists generated reports newest first.")
    public ApiResponse<List<ReportResponse>> history() {
        return ApiResponse.of(reportingService.listHistory());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Report status", description = "Returns metadata for a single report.")
    public ApiResponse<ReportResponse> get(@PathVariable UUID id) {
        return ApiResponse.of(reportingService.getReport(id));
    }

    @GetMapping("/{id}/download")
    @Operation(summary = "Download report", description = "Returns the generated PDF or Excel file bytes.")
    public ResponseEntity<byte[]> download(@PathVariable UUID id) {
        ReportFileDownload file = reportingService.download(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.fileName() + "\"")
                .contentType(MediaType.parseMediaType(file.contentType()))
                .body(file.content());
    }
}
