package com.projectanalytics.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Catalogued application error codes (see {@code docs/18_Error_Catalog.md}).
 *
 * <p>New codes must be added to the Error Catalog before use.
 */
public enum ErrorCode {

    AUTH_001("AUTH_001", "Invalid credentials.", HttpStatus.UNAUTHORIZED),
    AUTH_002("AUTH_002", "JWT token expired.", HttpStatus.UNAUTHORIZED),
    AUTH_003("AUTH_003", "JWT token invalid.", HttpStatus.UNAUTHORIZED),
    AUTH_004("AUTH_004", "Authentication required.", HttpStatus.UNAUTHORIZED),
    AUTH_005("AUTH_005", "Access denied.", HttpStatus.FORBIDDEN),
    AUTH_006("AUTH_006", "Insufficient permissions.", HttpStatus.FORBIDDEN),

    USER_001("USER_001", "User not found.", HttpStatus.NOT_FOUND),
    USER_002("USER_002", "Email already exists.", HttpStatus.CONFLICT),
    USER_003("USER_003", "Username already exists.", HttpStatus.CONFLICT),
    USER_004("USER_004", "Invalid password.", HttpStatus.BAD_REQUEST),
    USER_005("USER_005", "Invalid user preferences.", HttpStatus.BAD_REQUEST),

    SYNC_001("SYNC_001", "Synchronization failed.", HttpStatus.INTERNAL_SERVER_ERROR),
    SYNC_002("SYNC_002", "OpenProject timeout.", HttpStatus.REQUEST_TIMEOUT),
    SYNC_003("SYNC_003", "Synchronization already running.", HttpStatus.CONFLICT),
    SYNC_004("SYNC_004", "OpenProject unavailable.", HttpStatus.SERVICE_UNAVAILABLE),
    SYNC_005("SYNC_005", "Invalid synchronization request.", HttpStatus.BAD_REQUEST),
    SYNC_006("SYNC_006", "Data mapping failed.", HttpStatus.INTERNAL_SERVER_ERROR),

    WORKSPACE_001("WORKSPACE_001", "Workspace not found.", HttpStatus.NOT_FOUND),
    WORKSPACE_002("WORKSPACE_002", "Workspace URL already exists.", HttpStatus.CONFLICT),

    PORTFOLIO_001("PORTFOLIO_001", "Portfolio not found.", HttpStatus.NOT_FOUND),
    PORTFOLIO_002("PORTFOLIO_002", "Portfolio already exists.", HttpStatus.CONFLICT),
    PORTFOLIO_003("PORTFOLIO_003", "Invalid portfolio configuration.", HttpStatus.BAD_REQUEST),

    PROJECT_001("PROJECT_001", "Project not found.", HttpStatus.NOT_FOUND),

    ANALYTICS_001("ANALYTICS_001", "Health score calculation failed.", HttpStatus.INTERNAL_SERVER_ERROR),
    ANALYTICS_002("ANALYTICS_002", "Risk score calculation failed.", HttpStatus.INTERNAL_SERVER_ERROR),
    ANALYTICS_003("ANALYTICS_003", "Attention score calculation failed.", HttpStatus.INTERNAL_SERVER_ERROR),
    ANALYTICS_004("ANALYTICS_004", "Recommendation generation failed.", HttpStatus.INTERNAL_SERVER_ERROR),
    ANALYTICS_005("ANALYTICS_005", "Analytics unavailable.", HttpStatus.NOT_FOUND),

    REPORT_001("REPORT_001", "Report not found.", HttpStatus.NOT_FOUND),
    REPORT_002("REPORT_002", "Report generation failed.", HttpStatus.INTERNAL_SERVER_ERROR),
    REPORT_003("REPORT_003", "Report already exists.", HttpStatus.CONFLICT),
    REPORT_004("REPORT_004", "Report export failed.", HttpStatus.INTERNAL_SERVER_ERROR),

    RECOMMENDATION_001("RECOMMENDATION_001", "Recommendation not found.", HttpStatus.NOT_FOUND),

    VALIDATION_001("VALIDATION_001", "Required field missing.", HttpStatus.BAD_REQUEST),
    VALIDATION_002("VALIDATION_002", "Invalid request format.", HttpStatus.BAD_REQUEST),
    VALIDATION_003("VALIDATION_003", "Invalid parameter value.", HttpStatus.BAD_REQUEST),
    VALIDATION_004("VALIDATION_004", "Constraint violation.", HttpStatus.BAD_REQUEST),
    VALIDATION_005("VALIDATION_005", "Unsupported enum value.", HttpStatus.BAD_REQUEST),

    SYSTEM_001("SYSTEM_001", "Unexpected server error.", HttpStatus.INTERNAL_SERVER_ERROR),
    SYSTEM_002("SYSTEM_002", "Service unavailable.", HttpStatus.SERVICE_UNAVAILABLE),
    SYSTEM_003("SYSTEM_003", "Database error.", HttpStatus.INTERNAL_SERVER_ERROR),
    SYSTEM_004("SYSTEM_004", "Cache unavailable.", HttpStatus.INTERNAL_SERVER_ERROR),
    SYSTEM_005("SYSTEM_005", "Internal configuration error.", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String code;
    private final String defaultMessage;
    private final HttpStatus httpStatus;

    ErrorCode(String code, String defaultMessage, HttpStatus httpStatus) {
        this.code = code;
        this.defaultMessage = defaultMessage;
        this.httpStatus = httpStatus;
    }

    public String getCode() {
        return code;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
