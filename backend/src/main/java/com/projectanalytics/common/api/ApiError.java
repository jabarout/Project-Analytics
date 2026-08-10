package com.projectanalytics.common.api;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Collections;
import java.util.List;

/**
 * Error body nested inside the standard error envelope (Error Catalog).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiError(
        String code,
        String message,
        List<String> details
) {

    public ApiError(String code, String message) {
        this(code, message, Collections.emptyList());
    }
}
