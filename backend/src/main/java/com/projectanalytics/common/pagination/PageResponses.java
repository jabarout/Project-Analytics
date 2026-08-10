package com.projectanalytics.common.pagination;

import com.projectanalytics.common.api.PageResponse;
import org.springframework.data.domain.Page;

/**
 * Helper to map Spring Data {@link Page} into the API {@link PageResponse}.
 */
public final class PageResponses {

    private PageResponses() {
    }

    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast()
        );
    }
}
