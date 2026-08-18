package com.projectanalytics.synchronization.application;

import com.projectanalytics.analytics.persistence.AnalyticsRepository;
import com.projectanalytics.analytics.persistence.AnalyticsSnapshotRepository;
import com.projectanalytics.common.exception.BusinessException;
import com.projectanalytics.common.exception.ErrorCode;
import com.projectanalytics.infrastructure.openproject.OpenProjectClient;
import com.projectanalytics.infrastructure.openproject.OpenProjectConnectionProperties;
import com.projectanalytics.infrastructure.openproject.OpenProjectCredentialResolver;
import com.projectanalytics.infrastructure.openproject.dto.OpenProjectProjectDto;
import com.projectanalytics.infrastructure.openproject.dto.OpenProjectWorkPackageDto;
import com.projectanalytics.portfolio.persistence.PortfolioProjectRepository;
import com.projectanalytics.project.persistence.ProjectEntity;
import com.projectanalytics.project.persistence.ProjectRepository;
import com.projectanalytics.project.persistence.WorkPackageEntity;
import com.projectanalytics.project.persistence.WorkPackageRepository;
import com.projectanalytics.recommendation.persistence.RecommendationRepository;
import com.projectanalytics.synchronization.domain.SynchronizationType;
import com.projectanalytics.synchronization.persistence.WorkspaceEntity;
import com.projectanalytics.synchronization.persistence.WorkspaceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Transactional persistence of synchronized operational data.
 * Projects are owned by the workspace. Portfolio membership is optional and managed separately.
 * <p>
 * Each sync performs a <strong>full reconcile</strong> against OpenProject:
 * upsert current remote projects/work packages, then delete local rows that no longer exist remotely.
 * Incremental {@code modifiedSince} filtering alone cannot detect deletions.
 */
@Service
public class OperationalDataImportService {

    private static final Logger log = LoggerFactory.getLogger(OperationalDataImportService.class);

    private final WorkspaceRepository workspaceRepository;
    private final ProjectRepository projectRepository;
    private final WorkPackageRepository workPackageRepository;
    private final PortfolioProjectRepository portfolioProjectRepository;
    private final RecommendationRepository recommendationRepository;
    private final AnalyticsRepository analyticsRepository;
    private final AnalyticsSnapshotRepository analyticsSnapshotRepository;
    private final OpenProjectClient openProjectClient;
    private final OpenProjectCredentialResolver credentialResolver;

    public OperationalDataImportService(
            WorkspaceRepository workspaceRepository,
            ProjectRepository projectRepository,
            WorkPackageRepository workPackageRepository,
            PortfolioProjectRepository portfolioProjectRepository,
            RecommendationRepository recommendationRepository,
            AnalyticsRepository analyticsRepository,
            AnalyticsSnapshotRepository analyticsSnapshotRepository,
            OpenProjectClient openProjectClient,
            OpenProjectCredentialResolver credentialResolver
    ) {
        this.workspaceRepository = workspaceRepository;
        this.projectRepository = projectRepository;
        this.workPackageRepository = workPackageRepository;
        this.portfolioProjectRepository = portfolioProjectRepository;
        this.recommendationRepository = recommendationRepository;
        this.analyticsRepository = analyticsRepository;
        this.analyticsSnapshotRepository = analyticsSnapshotRepository;
        this.openProjectClient = openProjectClient;
        this.credentialResolver = credentialResolver;
    }

    @Transactional
    public ImportCounts importOperationalData(UUID workspaceId, SynchronizationType storedType) {
        WorkspaceEntity workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new BusinessException(ErrorCode.WORKSPACE_001));

        OpenProjectConnectionProperties connection =
                credentialResolver.resolve(workspaceId, workspace.getBaseUrl());

        // Full catalog fetch — required to detect remote deletions (modifiedSince cannot).
        String version = openProjectClient.fetchServerVersion(connection);
        List<OpenProjectProjectDto> remoteProjects = openProjectClient.fetchProjects(connection, null);

        int projectCount = 0;
        int workPackageCount = 0;
        int deletedWorkPackages = 0;
        Instant synchronizedAt = Instant.now();
        Set<Long> remoteProjectIds = new HashSet<>();

        for (OpenProjectProjectDto remoteProject : remoteProjects) {
            remoteProjectIds.add(remoteProject.id());
            ProjectEntity project = upsertProject(workspace, remoteProject, synchronizedAt);
            projectCount++;

            List<OpenProjectWorkPackageDto> remoteWorkPackages = openProjectClient.fetchWorkPackages(
                    connection,
                    remoteProject.id(),
                    null
            );
            Set<Long> remoteWpIds = new HashSet<>();
            for (OpenProjectWorkPackageDto remoteWorkPackage : remoteWorkPackages) {
                remoteWpIds.add(remoteWorkPackage.id());
                upsertWorkPackage(project, remoteWorkPackage, synchronizedAt);
                workPackageCount++;
            }
            deletedWorkPackages += reconcileWorkPackages(project, remoteWpIds);
        }

        int deletedProjects = reconcileProjects(workspace.getId(), remoteProjectIds);

        int adminsApplied = applyProjectAdmins(workspace, connection);
        log.info(
                "Sync workspace={} type={} projectsUpserted={} workPackagesUpserted={} "
                        + "projectsDeleted={} workPackagesDeleted={} projectAdminsApplied={}",
                workspaceId,
                storedType,
                projectCount,
                workPackageCount,
                deletedProjects,
                deletedWorkPackages,
                adminsApplied
        );

        return new ImportCounts(projectCount, workPackageCount, version, deletedProjects, deletedWorkPackages);
    }

    /**
     * Removes local work packages for a project that are no longer present in OpenProject.
     */
    private int reconcileWorkPackages(ProjectEntity project, Set<Long> remoteOpenProjectIds) {
        List<WorkPackageEntity> local = workPackageRepository.findByProjectId(project.getId());
        if (local.isEmpty()) {
            return 0;
        }
        if (remoteOpenProjectIds.isEmpty()) {
            workPackageRepository.deleteByProjectId(project.getId());
            return local.size();
        }
        List<WorkPackageEntity> stale = local.stream()
                .filter(wp -> !remoteOpenProjectIds.contains(wp.getOpenProjectId()))
                .toList();
        if (stale.isEmpty()) {
            return 0;
        }
        workPackageRepository.deleteAll(stale);
        return stale.size();
    }

    /**
     * Removes local projects (and dependent analytics/membership/recommendations) absent from OpenProject.
     */
    private int reconcileProjects(UUID workspaceId, Set<Long> remoteOpenProjectIds) {
        List<ProjectEntity> localProjects = projectRepository.findByWorkspaceIdOrderByNameAsc(workspaceId);
        List<ProjectEntity> stale = localProjects.stream()
                .filter(p -> !remoteOpenProjectIds.contains(p.getOpenProjectId()))
                .toList();
        for (ProjectEntity project : stale) {
            deleteLocalProjectCascade(project);
        }
        return stale.size();
    }

    private void deleteLocalProjectCascade(ProjectEntity project) {
        UUID projectId = project.getId();
        recommendationRepository.deleteByProjectId(projectId);
        analyticsSnapshotRepository.deleteByProjectId(projectId);
        analyticsRepository.deleteByProjectId(projectId);
        workPackageRepository.deleteByProjectId(projectId);
        portfolioProjectRepository.deleteByProjectId(projectId);
        projectRepository.delete(project);
        log.info(
                "Removed local project id={} openProjectId={} name={} (no longer in OpenProject)",
                projectId,
                project.getOpenProjectId(),
                project.getName()
        );
    }

    /**
     * Updates {@code admin_name} on every local project in the workspace from OpenProject memberships.
     */
    private int applyProjectAdmins(WorkspaceEntity workspace, OpenProjectConnectionProperties connection) {
        Map<Long, List<String>> adminsByOpenProjectId =
                openProjectClient.fetchProjectAdminNamesByProjectId(connection);
        if (adminsByOpenProjectId == null) {
            adminsByOpenProjectId = Map.of();
        }
        int updated = 0;
        for (ProjectEntity project : projectRepository.findByWorkspaceIdOrderByNameAsc(workspace.getId())) {
            List<String> admins = adminsByOpenProjectId.get(project.getOpenProjectId());
            String adminName = (admins == null || admins.isEmpty()) ? null : String.join(", ", admins);
            if (adminName == null && project.getAdminName() == null) {
                continue;
            }
            if (adminName != null && adminName.equals(project.getAdminName())) {
                continue;
            }
            project.setAdminName(adminName);
            projectRepository.save(project);
            updated++;
        }
        return updated;
    }

    private ProjectEntity upsertProject(
            WorkspaceEntity workspace,
            OpenProjectProjectDto remote,
            Instant synchronizedAt
    ) {
        ProjectEntity project = projectRepository
                .findByWorkspaceIdAndOpenProjectId(workspace.getId(), remote.id())
                .orElseGet(() -> new ProjectEntity(workspace, remote.id(), remote.name()));
        project.setName(remote.name());
        project.setDescription(remote.description());
        project.setStatus(remote.status());
        project.setStartDate(remote.startDate());
        project.setEndDate(remote.endDate());
        project.setAdminName(remote.adminName());
        project.setSynchronizedAt(synchronizedAt);
        return projectRepository.save(project);
    }

    private void upsertWorkPackage(
            ProjectEntity project,
            OpenProjectWorkPackageDto remote,
            Instant synchronizedAt
    ) {
        WorkPackageEntity workPackage = workPackageRepository
                .findByProjectIdAndOpenProjectId(project.getId(), remote.id())
                .orElseGet(() -> new WorkPackageEntity(project, remote.id(), remote.subject()));
        workPackage.setSubject(remote.subject());
        workPackage.setType(remote.type());
        workPackage.setStatus(remote.status());
        workPackage.setPriority(remote.priority());
        workPackage.setAssignee(remote.assignee());
        workPackage.setEstimatedHours(remote.estimatedHours());
        workPackage.setSpentHours(remote.spentHours());
        workPackage.setDueDate(remote.dueDate());
        workPackage.setSynchronizedAt(synchronizedAt);
        workPackageRepository.save(workPackage);
    }

    public record ImportCounts(
            int projects,
            int workPackages,
            String openProjectVersion,
            int deletedProjects,
            int deletedWorkPackages
    ) {
    }
}
