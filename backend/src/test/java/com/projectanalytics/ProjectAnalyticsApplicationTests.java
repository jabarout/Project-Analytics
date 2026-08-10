package com.projectanalytics;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Smoke test that the Spring application context boots under the test profile.
 * Detailed infrastructure checks live in {@link com.projectanalytics.infrastructure.InfrastructureIntegrationTest}.
 */
@SpringBootTest
@ActiveProfiles("test")
class ProjectAnalyticsApplicationTests {

    @Test
    void contextLoads() {
        // Application startup smoke test for Milestone 1 foundation.
    }
}
