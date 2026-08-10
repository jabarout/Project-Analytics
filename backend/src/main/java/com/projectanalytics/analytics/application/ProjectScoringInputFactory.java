package com.projectanalytics.analytics.application;

import com.projectanalytics.analytics.domain.ProjectScoringInput;
import com.projectanalytics.project.persistence.ProjectEntity;
import com.projectanalytics.project.persistence.WorkPackageEntity;
import com.projectanalytics.project.persistence.WorkPackageRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Builds scoring inputs exclusively from local project and work-package rows.
 */
@Component
public class ProjectScoringInputFactory {

    private static final Set<String> COMPLETED_STATUSES = Set.of(
            "CLOSED", "DONE", "RESOLVED", "REJECTED", "COMPLETED"
    );

    private final WorkPackageRepository workPackageRepository;

    public ProjectScoringInputFactory(WorkPackageRepository workPackageRepository) {
        this.workPackageRepository = workPackageRepository;
    }

    public ProjectScoringInput fromProject(ProjectEntity project) {
        List<WorkPackageEntity> workPackages = workPackageRepository.findByProjectId(project.getId());
        LocalDate today = LocalDate.now();

        long total = workPackages.size();
        long completed = workPackages.stream().filter(wp -> isCompleted(wp.getStatus())).count();
        long open = total - completed;

        List<Long> overdueAges = workPackages.stream()
                .filter(wp -> !isCompleted(wp.getStatus()))
                .filter(wp -> wp.getDueDate() != null && wp.getDueDate().isBefore(today))
                .map(wp -> ChronoUnit.DAYS.between(wp.getDueDate(), today))
                .toList();
        long overdue = overdueAges.size();
        BigDecimal avgOverdueAgeDays = null;
        Integer maxOverdueAgeDays = null;
        if (!overdueAges.isEmpty()) {
            long sum = overdueAges.stream().mapToLong(Long::longValue).sum();
            avgOverdueAgeDays = BigDecimal.valueOf(sum)
                    .divide(BigDecimal.valueOf(overdueAges.size()), 2, RoundingMode.HALF_UP);
            maxOverdueAgeDays = overdueAges.stream().mapToInt(Long::intValue).max().orElse(0);
        }

        long highPriorityOpen = workPackages.stream()
                .filter(wp -> !isCompleted(wp.getStatus()))
                .filter(wp -> isHighPriority(wp.getPriority()))
                .count();

        return new ProjectScoringInput(
                project.getId(),
                project.getName(),
                project.getStatus(),
                project.getProgress(),
                project.getBudget(),
                project.getStartDate(),
                project.getEndDate(),
                total,
                completed,
                open,
                overdue,
                highPriorityOpen,
                avgOverdueAgeDays,
                maxOverdueAgeDays,
                today
        );
    }

    private static boolean isCompleted(String status) {
        if (status == null || status.isBlank()) {
            return false;
        }
        return COMPLETED_STATUSES.contains(status.trim().toUpperCase(Locale.ROOT));
    }

    private static boolean isHighPriority(String priority) {
        if (priority == null || priority.isBlank()) {
            return false;
        }
        String normalized = priority.trim().toUpperCase(Locale.ROOT);
        return normalized.contains("HIGH") || normalized.contains("IMMEDIATE") || normalized.contains("URGENT");
    }
}
