package com.projectanalytics.analytics.scoring;

import com.projectanalytics.analytics.domain.ProjectScoringInput;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ProgressMetricsTest {

    @Test
    void actualProgressPrefersWorkPackageRatioOverProjectProgressField() {
        ProjectScoringInput input = base(
                new BigDecimal("90.00"),
                LocalDate.now().minusDays(30),
                LocalDate.now().plusDays(30),
                4,
                1,
                0
        );

        // 1/4 completed → 25%, not the OP project field 90%
        assertThat(ProgressMetrics.actualProgress(input)).isEqualByComparingTo("25.00");
    }

    @Test
    void actualProgressFallsBackToProjectProgressWhenNoWorkPackages() {
        ProjectScoringInput input = base(
                new BigDecimal("42.50"),
                LocalDate.now().minusDays(10),
                LocalDate.now().plusDays(10),
                0,
                0,
                0
        );

        assertThat(ProgressMetrics.actualProgress(input)).isEqualByComparingTo("42.50");
    }

    @Test
    void expectedAndGapReflectMidSchedule() {
        LocalDate asOf = LocalDate.of(2026, 6, 15);
        ProjectScoringInput input = new ProjectScoringInput(
                UUID.randomUUID(),
                "Mid",
                "ACTIVE",
                null,
                null,
                LocalDate.of(2026, 6, 1),
                LocalDate.of(2026, 6, 29),
                10,
                2,
                8,
                0,
                0,
                null,
                null,
                asOf
        );

        // 14 elapsed / 28 total = 50% expected; actual 20%; gap -30
        assertThat(ProgressMetrics.expectedProgress(input)).isEqualByComparingTo("50.00");
        assertThat(ProgressMetrics.actualProgress(input)).isEqualByComparingTo("20.00");
        assertThat(ProgressMetrics.progressGap(input)).isEqualByComparingTo("-30.00");
    }

    @Test
    void expectedProgressNullWhenDatesIncomplete() {
        ProjectScoringInput input = base(null, null, LocalDate.now().plusDays(10), 2, 1, 0);
        assertThat(ProgressMetrics.expectedProgress(input)).isNull();
        assertThat(ProgressMetrics.progressGap(input)).isNull();
    }

    @Test
    void overdueRatioAndScheduleVarianceReuseExistingSemantics() {
        LocalDate asOf = LocalDate.of(2026, 8, 10);
        ProjectScoringInput input = new ProjectScoringInput(
                UUID.randomUUID(),
                "Late",
                "ACTIVE",
                null,
                null,
                LocalDate.of(2026, 7, 1),
                LocalDate.of(2026, 8, 20),
                10,
                3,
                7,
                2,
                1,
                new BigDecimal("5.00"),
                8,
                asOf
        );

        assertThat(ProgressMetrics.overdueRatio(input)).isEqualByComparingTo("0.2000");
        // end 20 Aug, asOf 10 Aug → 10 days remaining → variance -10
        assertThat(ProgressMetrics.scheduleVarianceDays(input)).isEqualByComparingTo("-10.00");
    }

    private static ProjectScoringInput base(
            BigDecimal progress,
            LocalDate start,
            LocalDate end,
            long total,
            long completed,
            long overdue
    ) {
        return new ProjectScoringInput(
                UUID.randomUUID(),
                "P",
                "ACTIVE",
                progress,
                null,
                start,
                end,
                total,
                completed,
                total - completed,
                overdue,
                0,
                overdue > 0 ? new BigDecimal("3.00") : null,
                overdue > 0 ? 5 : null,
                LocalDate.now()
        );
    }
}
