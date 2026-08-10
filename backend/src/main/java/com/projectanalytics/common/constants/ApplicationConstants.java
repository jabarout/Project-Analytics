package com.projectanalytics.common.constants;

/**
 * Shared application-wide constants.
 *
 * <p>Business thresholds and scoring weights must not live here; those are externalized
 * configuration introduced in later milestones.
 */
public final class ApplicationConstants {

    public static final String API_V1_BASE_PATH = "/api/v1";

    public static final String DEFAULT_PAGE_SORT = "createdAt";

    public static final int DEFAULT_PAGE_SIZE = 20;

    public static final int MAX_PAGE_SIZE = 100;

    public static final String REQUEST_ID_HEADER = "X-Request-Id";

    public static final String REQUEST_ID_MDC_KEY = "requestId";

    private ApplicationConstants() {
    }
}
