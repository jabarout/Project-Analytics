package com.projectanalytics.common.api;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;

/**
 * Standard error API envelope as defined in the Error Catalog.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        boolean success,
        ApiError error,
        Instant timestamp,
        String path
) {

    public static ErrorResponse of(String code, String message, String path) {
        return new ErrorResponse(false, new ApiError(code, message), Instant.now(), path);
    }

    public static ErrorResponse of(String code, String message, java.util.List<String> details, String path) {
        return new ErrorResponse(false, new ApiError(code, message, details), Instant.now(), path);
    }
}
