package com.projectanalytics.synchronization.application;

import com.projectanalytics.analytics.persistence.AnalyticsRepository;
import com.projectanalytics.analytics.persistence.AnalyticsSnapshotRepository;
import com.projectanalytics.common.exception.BusinessException;
import com.projectanalytics.common.exception.ErrorCode;
import com.projectanalytics.infrastructure.openproject.OpenProjectProperties;
import com.projectanalytics.portfolio.persistence.PortfolioEntity;
import com.projectanalytics.portfolio.persistence.PortfolioProjectRepository;
import com.projectanalytics.portfolio.persistence.PortfolioRepository;
import com.projectanalytics.project.persistence.ProjectEntity;
import com.projectanalytics.project.persistence.ProjectRepository;
import com.projectanalytics.project.persistence.WorkPackageRepository;
import com.projectanalytics.recommendation.persistence.RecommendationRepository;
import com.projectanalytics.synchronization.api.dto.CreateWorkspaceRequest;
import com.projectanalytics.synchronization.api.dto.UpdateWorkspaceRequest;
import com.projectanalytics.synchronization.api.dto.WorkspaceResponse;
import com.projectanalytics.synchronization.persistence.SynchronizationHistoryRepository;
import com.projectanalytics.synchronization.persistence.WorkspaceEntity;
import com.projectanalytics.synchronization.persistence.WorkspaceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class WorkspaceService {

    private final WorkspaceRepository workspaceRepository;
    private final OpenProjectProperties openProjectProperties;
    private final ProjectRepository projectRepository;
    private final WorkPackageRepository workPackageRepository;
    private final AnalyticsRepository analyticsRepository;
    private final AnalyticsSnapshotRepository analyticsSnapshotRepository;
    private final PortfolioRepository portfolioRepository;
    private final PortfolioProjectRepository portfolioProjectRepository;
    private final SynchronizationHistoryRepository synchronizationHistoryRepository;
    private final RecommendationRepository recommendationRepository;
    private final WorkspaceAccessService workspaceAccessService;

    public WorkspaceService(
            WorkspaceRepository workspaceRepository,
            OpenProjectProperties openProjectProperties,
            ProjectRepository projectRepository,
            WorkPackageRepository workPackageRepository,
            AnalyticsRepository analyticsRepository,
            AnalyticsSnapshotRepository analyticsSnapshotRepository,
            PortfolioRepository portfolioRepository,
            PortfolioProjectRepository portfolioProjectRepository,
            SynchronizationHistoryRepository synchronizationHistoryRepository,
            RecommendationRepository recommendationRepository,
            WorkspaceAccessService workspaceAccessService
    ) {
        this.workspaceRepository = workspaceRepository;
        this.openProjectProperties = openProjectProperties;
        this.projectRepository = projectRepository;
        this.workPackageRepository = workPackageRepository;
        this.analyticsRepository = analyticsRepository;
        this.analyticsSnapshotRepository = analyticsSnapshotRepository;
        this.portfolioRepository = portfolioRepository;
        this.portfolioProjectRepository = portfolioProjectRepository;
        this.synchronizationHistoryRepository = synchronizationHistoryRepository;
        this.recommendationRepository = recommendationRepository;
        this.workspaceAccessService = workspaceAccessService;
    }

    @Transactional(readOnly = true)
    public List<WorkspaceResponse> listWorkspaces(UUID userId) {
        Set<UUID> allowed = new HashSet<>(workspaceAccessService.workspaceIdsWithAnalyticsAccess(userId));
        return workspaceRepository.findAll().stream()
                .filter(workspace -> allowed.contains(workspace.getId()))
                .map(workspace -> toResponse(workspace, userId))
                .toList();
    }

    @Transactional(readOnly = true)
    public WorkspaceResponse getWorkspace(UUID id, UUID userId) {
        workspaceAccessService.requireAnalyticsAccess(id, userId);
        return toResponse(requireWorkspace(id), userId);
    }

    @Transactional
    public WorkspaceResponse createWorkspace(CreateWorkspaceRequest request) {
        String baseUrl = resolveBaseUrl(request.baseUrl());
        if (workspaceRepository.existsByBaseUrlIgnoreCase(baseUrl)) {
            throw new BusinessException(ErrorCode.WORKSPACE_002);
        }
        WorkspaceEntity workspace = new WorkspaceEntity(request.name().trim(), baseUrl);
        // Legacy path — no caller context; flags default false until membership exists.
        return toResponse(workspaceRepository.save(workspace), null);
    }

    @Transactional
    public WorkspaceResponse updateWorkspace(UUID id, UpdateWorkspaceRequest request, UUID userId) {
        WorkspaceEntity workspace = requireWorkspace(id);
        workspace.setName(request.name().trim());
        return toResponse(workspaceRepository.save(workspace), userId);
    }

    /**
     * Disconnects OpenProject connection and purges local synchronized analytics for the workspace.
     */
    @Transactional
    public void deleteWorkspace(UUID id) {
        WorkspaceEntity workspace = requireWorkspace(id);
        List<ProjectEntity> projects = projectRepository.findByWorkspaceIdOrderByNameAsc(id);
        for (ProjectEntity project : projects) {
            recommendationRepository.deleteByProjectId(project.getId());
        }
        analyticsSnapshotRepository.deleteByWorkspaceId(id);
        analyticsRepository.deleteByWorkspaceId(id);
        workPackageRepository.deleteByProjectWorkspaceId(id);

        List<PortfolioEntity> portfolios = portfolioRepository.findByWorkspaceIdOrderByNameAsc(id);
        for (PortfolioEntity portfolio : portfolios) {
            portfolioProjectRepository.deleteByPortfolioId(portfolio.getId());
        }
        portfolioRepository.deleteAll(portfolios);
        projectRepository.deleteAll(projects);
        synchronizationHistoryRepository.deleteByWorkspaceId(id);
        workspaceRepository.delete(workspace);
    }

    private WorkspaceEntity requireWorkspace(UUID id) {
        return workspaceRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.WORKSPACE_001));
    }

    private String resolveBaseUrl(String requested) {
        String candidate = (requested == null || requested.isBlank())
                ? openProjectProperties.getUrl()
                : requested.trim();
        if (candidate == null || candidate.isBlank()) {
            throw new BusinessException(ErrorCode.SYNC_005, "Workspace base URL is required.");
        }
        while (candidate.endsWith("/")) {
            candidate = candidate.substring(0, candidate.length() - 1);
        }
        return candidate;
    }

    private WorkspaceResponse toResponse(WorkspaceEntity entity, UUID userId) {
        boolean workspaceAdmin = userId != null
                && workspaceAccessService.isWorkspaceAdmin(entity.getId(), userId);
        boolean analyticsAccess = userId != null
                && workspaceAccessService.hasAnalyticsAccess(entity.getId(), userId);
        return new WorkspaceResponse(
                entity.getId(),
                entity.getName(),
                entity.getBaseUrl(),
                entity.getVersion(),
                entity.getSynchronizationStatus().name(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                workspaceAdmin,
                analyticsAccess
        );
    }
}
