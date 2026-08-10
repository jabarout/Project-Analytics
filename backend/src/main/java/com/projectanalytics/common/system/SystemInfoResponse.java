package com.projectanalytics.common.system;

/**
 * Basic runtime information for the foundation health surface.
 */
public record SystemInfoResponse(
        String application,
        String version,
        String environment,
        String apiVersion
) {
}
