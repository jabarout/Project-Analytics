package com.projectanalytics.common.api;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;

/**
 * Standard successful API envelope as defined in the API specification.
 *
 * @param <T> payload type
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        boolean success,
        T data,
        Instant timestamp
) {

    public static <T> ApiResponse<T> of(T data) {
        return new ApiResponse<>(true, data, Instant.now());
    }
}
