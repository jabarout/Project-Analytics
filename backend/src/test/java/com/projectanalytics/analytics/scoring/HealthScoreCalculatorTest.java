package com.projectanalytics.analytics.scoring;

import com.projectanalytics.analytics.configuration.AnalyticsScoringProperties;
import com.projectanalytics.analytics.domain.ProjectScoringInput;
import com.projectanalytics.analytics.domain.ScoredMetric;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class HealthScoreCalculatorTest {

    private HealthScoreCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new HealthScoreCalculator(new AnalyticsScoringProperties());
    }

    @Test
    void healthyProjectScoresHigherThanOverdueProject() {
        ProjectScoringInput healthy = new ProjectScoringInput(
                UUID.randomUUID(),
                "Healthy",
                "ACTIVE",
                new BigDecimal("80"),
                null,
                LocalDate.now().minusDays(10),
                LocalDate.now().plusDays(20),
                10,
                8,
                2,
                0,
                0,
                null,
                null,
                LocalDate.now()
        );
        ProjectScoringInput overdue = new ProjectScoringInput(
                UUID.randomUUID(),
                "Overdue",
                "ACTIVE",
                new BigDecimal("20"),
                null,
                LocalDate.now().minusDays(40),
                LocalDate.now().minusDays(5),
                10,
                1,
                9,
                7,
                3,
                new BigDecimal("12.00"),
                20,
                LocalDate.now()
        );

        ScoredMetric healthyScore = calculator.calculate(healthy);
        ScoredMetric overdueScore = calculator.calculate(overdue);

        assertThat(healthyScore.score()).isGreaterThan(overdueScore.score());
        assertThat(healthyScore.explanation()).isNotBlank();
        assertThat(overdueScore.explanation().toLowerCase()).containsAnyOf("overdue", "past due", "behind");
    }
}
