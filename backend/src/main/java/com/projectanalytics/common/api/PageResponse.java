package com.projectanalytics.common.api;

import java.util.List;

/**
 * Generic paginated payload for list endpoints.
 *
 * @param <T> item type
 */
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last
) {
}
