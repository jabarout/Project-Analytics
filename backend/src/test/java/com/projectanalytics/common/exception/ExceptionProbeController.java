package com.projectanalytics.common.exception;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Test-only controller used to exercise {@link GlobalExceptionHandler}.
 */
@RestController
@RequestMapping("/api/v1/test")
class ExceptionProbeController {

    @GetMapping("/business-error")
    void throwBusinessError() {
        throw new BusinessException(ErrorCode.SYSTEM_002);
    }
}
