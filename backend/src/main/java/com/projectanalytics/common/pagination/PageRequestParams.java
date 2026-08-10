package com.projectanalytics.common.pagination;

import com.projectanalytics.common.constants.ApplicationConstants;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * Shared query parameters for paginated list endpoints.
 */
public record PageRequestParams(
        @Min(0) Integer page,
        @Min(1) @Max(ApplicationConstants.MAX_PAGE_SIZE) Integer size,
        String sort,
        String direction
) {

    public Pageable toPageable() {
        int resolvedPage = page == null ? 0 : page;
        int resolvedSize = size == null ? ApplicationConstants.DEFAULT_PAGE_SIZE : size;
        String resolvedSort = (sort == null || sort.isBlank())
                ? ApplicationConstants.DEFAULT_PAGE_SORT
                : sort;
        Sort.Direction resolvedDirection = "desc".equalsIgnoreCase(direction)
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;
        return PageRequest.of(resolvedPage, resolvedSize, Sort.by(resolvedDirection, resolvedSort));
    }
}
