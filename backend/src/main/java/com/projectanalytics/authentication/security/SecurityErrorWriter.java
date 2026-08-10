package com.projectanalytics.authentication.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.projectanalytics.common.api.ErrorResponse;
import com.projectanalytics.common.exception.ErrorCode;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Writes Error Catalog responses for authentication/authorization failures.
 */
@Component
public class SecurityErrorWriter {

    private final ObjectMapper objectMapper;

    public SecurityErrorWriter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void write(HttpServletResponse response, String path, ErrorCode errorCode) throws IOException {
        if (response.isCommitted()) {
            return;
        }
        response.setStatus(errorCode.getHttpStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ErrorResponse body = ErrorResponse.of(errorCode.getCode(), errorCode.getDefaultMessage(), path);
        objectMapper.writeValue(response.getOutputStream(), body);
    }
}
