package com.projectanalytics.common.pagination;

import com.projectanalytics.common.constants.ApplicationConstants;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import static org.assertj.core.api.Assertions.assertThat;

class PageRequestParamsTest {

    @Test
    void toPageable_usesDefaultsWhenNull() {
        Pageable pageable = new PageRequestParams(null, null, null, null).toPageable();

        assertThat(pageable.getPageNumber()).isZero();
        assertThat(pageable.getPageSize()).isEqualTo(ApplicationConstants.DEFAULT_PAGE_SIZE);
        assertThat(pageable.getSort().getOrderFor(ApplicationConstants.DEFAULT_PAGE_SORT))
                .isNotNull()
                .extracting(Sort.Order::getDirection)
                .isEqualTo(Sort.Direction.ASC);
    }

    @Test
    void toPageable_appliesDescendingDirection() {
        Pageable pageable = new PageRequestParams(2, 50, "name", "desc").toPageable();

        assertThat(pageable.getPageNumber()).isEqualTo(2);
        assertThat(pageable.getPageSize()).isEqualTo(50);
        assertThat(pageable.getSort().getOrderFor("name").getDirection()).isEqualTo(Sort.Direction.DESC);
    }
}
