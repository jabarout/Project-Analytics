package com.projectanalytics.common.exception;

import java.util.Collections;
import java.util.List;

/**
 * Base exception for documented business and system errors.
 *
 * <p>Prefer specific subclasses in feature modules. Always map to a catalogued {@link ErrorCode}.
 */
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;
    private final List<String> details;

    public BusinessException(ErrorCode errorCode) {
        this(errorCode, errorCode.getDefaultMessage(), Collections.emptyList());
    }

    public BusinessException(ErrorCode errorCode, String message) {
        this(errorCode, message, Collections.emptyList());
    }

    public BusinessException(ErrorCode errorCode, String message, List<String> details) {
        super(message);
        this.errorCode = errorCode;
        this.details = details == null ? Collections.emptyList() : List.copyOf(details);
    }

    public BusinessException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.details = Collections.emptyList();
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public List<String> getDetails() {
        return details;
    }
}
