package com.projectanalytics.common.system;

import com.projectanalytics.common.api.ApiResponse;
import com.projectanalytics.common.constants.ApplicationConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Foundation system endpoints (not domain business logic).
 */
@RestController
@RequestMapping(ApplicationConstants.API_V1_BASE_PATH + "/system")
@Tag(name = "System", description = "Application foundation and runtime information")
public class SystemController {

    private final String applicationName;
    private final String applicationVersion;
    private final String activeProfile;

    public SystemController(
            @Value("${spring.application.name}") String applicationName,
            @Value("${projectanalytics.info.version}") String applicationVersion,
            @Value("${spring.profiles.active:default}") String activeProfile
    ) {
        this.applicationName = applicationName;
        this.applicationVersion = applicationVersion;
        this.activeProfile = activeProfile;
    }

    @GetMapping("/info")
    @Operation(summary = "Application information", description = "Returns non-sensitive runtime metadata.")
    public ApiResponse<SystemInfoResponse> getSystemInfo() {
        return ApiResponse.of(new SystemInfoResponse(
                applicationName,
                applicationVersion,
                activeProfile,
                "v1"
        ));
    }
}
