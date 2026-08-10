package com.projectanalytics.synchronization.application;

import com.projectanalytics.common.exception.BusinessException;
import com.projectanalytics.common.exception.ErrorCode;
import com.projectanalytics.infrastructure.openproject.OpenProjectClient;
import com.projectanalytics.infrastructure.openproject.OpenProjectConnectionProperties;
import com.projectanalytics.infrastructure.openproject.OpenProjectCredentialResolver;
import com.projectanalytics.infrastructure.openproject.dto.OpenProjectProjectDto;
import com.projectanalytics.infrastructure.openproject.dto.OpenProjectWorkPackageDto;
import com.projectanalytics.project.persistence.ProjectEntity;
import com.projectanalytics.project.persistence.ProjectRepository;
import com.projectanalytics.project.persistence.WorkPackageEntity;
import com.projectanalytics.project.persistence.WorkPackageRepository;
import com.projectanalytics.synchronization.domain.SynchronizationStatus;
import com.projectanalytics.synchronization.domain.SynchronizationType;
import com.projectanalytics.synchronization.persistence.SynchronizationHistoryEntity;
import com.projectanalytics.synchronization.persistence.SynchronizationHistoryRepository;
import com.projectanalytics.synchronization.persistence.WorkspaceEntity;
import com.projectanalytics.synchronization.persistence.WorkspaceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Transactional persistence of synchronized operational data.
 * Projects are owned by the workspace. Portfolio membership is optional and managed separately.
 */
@Service
public class OperationalDataImportService {

    private static final Logger log = LoggerFactory.getLogger(OperationalDataImportService.class);

    private final WorkspaceRepository workspaceRepository;
    private final ProjectRepository projectRepository;
    private final WorkPackageRepository workPackageRepository;
    private final SynchronizationHistoryRepository historyRepository;
    private final OpenProjectClient openProjectClient;
    private final OpenProjectCredentialResolver credentialResolver;

    public OperationalDataImportService(
            WorkspaceRepository workspaceRepository,
            ProjectRepository projectRepository,
            WorkPackageRepository workPackageRepository,
            SynchronizationHistoryRepository historyRepository,
            OpenProjectClient openProjectClient,
            OpenProjectCredentialResolver credentialResolver
    ) {
        this.workspaceRepository = workspaceRepository;
        this.projectRepository = projectRepository;
        this.workPackageRepository = workPackageRepository;
        this.historyRepository = historyRepository;
        this.openProjectClient = openProjectClient;
        this.credentialResolver = credentialResolver;
    }

    @Transactional
    public ImportCounts importOperationalData(UUID workspaceId, SynchronizationType storedType) {
        WorkspaceEntity workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new BusinessException(ErrorCode.WORKSPACE_001));

        OpenProjectConnectionProperties connection =
                credentialResolver.resolve(workspaceId, workspace.getBaseUrl());
        Instant modifiedSince = resolveModifiedSince(workspaceId, storedType);

        String version = openProjectClient.fetchServerVersion(connection);
        List<OpenProjectProjectDto> remoteProjects =
                openProjectClient.fetchProjects(connection, modifiedSince);

        int projectCount = 0;
        int workPackageCount = 0;
        Instant synchronizedAt = Instant.now();

        for (OpenProjectProjectDto remoteProject : remoteProjects) {
            ProjectEntity project = upsertProject(workspace, remoteProject, synchronizedAt);
            projectCount++;

            List<OpenProjectWorkPackageDto> remoteWorkPackages = openProjectClient.fetchWorkPackages(
                    connection,
                    remoteProject.id(),
                    modifiedSince
            );
            for (OpenProjectWorkPackageDto remoteWorkPackage : remoteWorkPackages) {
                upsertWorkPackage(project, remoteWorkPackage, synchronizedAt);
                workPackageCount++;
            }
        }

        // Always refresh project admins for *all* local projects (not only incrementally returned ones).
        // Memberships are the source of "Project admin"; this must not depend on project modifiedSince.
        int adminsApplied = applyProjectAdmins(workspace, connection);
        log.info(
                "Sync workspace={} projectsUpserted={} workPackages={} projectAdminsApplied={}",
                workspaceId,
                projectCount,
                workPackageCount,
                adminsApplied
        );

        return new ImportCounts(projectCount, workPackageCount, version);
    }

    /**
     * Updates {@code admin_name} on every local project in the workspace from OpenProject memberships.
     */
    private int applyProjectAdmins(WorkspaceEntity workspace, OpenProjectConnectionProperties connection) {
        Map<Long, List<String>> adminsByOpenProjectId =
                openProjectClient.fetchProjectAdminNamesByProjectId(connection);
        int updated = 0;
        for (ProjectEntity project : projectRepository.findByWorkspaceIdOrderByNameAsc(workspace.getId())) {
            List<String> admins = adminsByOpenProjectId.get(project.getOpenProjectId());
            String adminName = (admins == null || admins.isEmpty()) ? null : String.join(", ", admins);
            // Always write so stale admins clear when memberships change.
            if (adminName == null && project.getAdminName() == null) {
                continue;
            }
            if (adminName != null && adminName.equals(project.getAdminName())) {
                continue;
            }
            if (adminName == null && project.getAdminName() != null) {
                project.setAdminName(null);
                projectRepository.save(project);
                updated++;
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

    private Instant resolveModifiedSince(UUID workspaceId, SynchronizationType type) {
        if (type == SynchronizationType.INITIAL) {
            return null;
        }
        return historyRepository
                .findFirstByWorkspaceIdAndStatusOrderByFinishedAtDesc(workspaceId, SynchronizationStatus.SUCCESS)
                .map(SynchronizationHistoryEntity::getFinishedAt)
                .orElse(null);
    }

    public record ImportCounts(int projects, int workPackages, String openProjectVersion) {
    }
}
