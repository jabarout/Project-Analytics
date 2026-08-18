package com.projectanalytics.portfolio.application;

import com.projectanalytics.analytics.api.dto.ScopeAnalyticsKpiResponse;
import com.projectanalytics.analytics.api.dto.ScopeDashboardResponse;
import com.projectanalytics.analytics.application.AnalyticsQueryService;
import com.projectanalytics.common.exception.BusinessException;
import com.projectanalytics.common.exception.ErrorCode;
import com.projectanalytics.portfolio.api.dto.AssignProjectRequest;
import com.projectanalytics.portfolio.api.dto.CreatePortfolioRequest;
import com.projectanalytics.portfolio.api.dto.PortfolioDetailResponse;
import com.projectanalytics.portfolio.api.dto.PortfolioProjectSummaryResponse;
import com.projectanalytics.portfolio.api.dto.PortfolioSummaryResponse;
import com.projectanalytics.portfolio.api.dto.UpdatePortfolioRequest;
import com.projectanalytics.portfolio.persistence.PortfolioEntity;
import com.projectanalytics.portfolio.persistence.PortfolioProjectEntity;
import com.projectanalytics.portfolio.persistence.PortfolioProjectRepository;
import com.projectanalytics.portfolio.persistence.PortfolioRepository;
import com.projectanalytics.project.persistence.ProjectEntity;
import com.projectanalytics.project.persistence.ProjectRepository;
import com.projectanalytics.synchronization.persistence.WorkspaceEntity;
import com.projectanalytics.synchronization.persistence.WorkspaceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Portfolio management against the local domain model only.
 * Portfolios are optional analytical collections (many-to-many membership).
 * Workspace owns projects; membership does not change ownership.
 */
@Service
public class PortfolioService {

    private final PortfolioRepository portfolioRepository;
    private final PortfolioProjectRepository portfolioProjectRepository;
    private final WorkspaceRepository workspaceRepository;
    private final ProjectRepository projectRepository;
    private final PortfolioLocalMetricsService metricsService;
    private final AnalyticsQueryService analyticsQueryService;

    public PortfolioService(
            PortfolioRepository portfolioRepository,
            PortfolioProjectRepository portfolioProjectRepository,
            WorkspaceRepository workspaceRepository,
            ProjectRepository projectRepository,
            PortfolioLocalMetricsService metricsService,
            AnalyticsQueryService analyticsQueryService
    ) {
        this.portfolioRepository = portfolioRepository;
        this.portfolioProjectRepository = portfolioProjectRepository;
        this.workspaceRepository = workspaceRepository;
        this.projectRepository = projectRepository;
        this.metricsService = metricsService;
        this.analyticsQueryService = analyticsQueryService;
    }

    @Transactional(readOnly = true)
    public List<PortfolioSummaryResponse> listPortfolios(UUID workspaceId) {
        List<PortfolioEntity> portfolios = workspaceId == null
                ? portfolioRepository.findAllByOrderByNameAsc()
                : portfolioRepository.findByWorkspaceIdOrderByNameAsc(workspaceId);
        return portfolios.stream().map(this::toSummary).toList();
    }

    @Transactional(readOnly = true)
    public PortfolioDetailResponse getPortfolio(UUID portfolioId) {
        PortfolioEntity portfolio = requirePortfolio(portfolioId);
        List<PortfolioProjectSummaryResponse> projects = metricsService.listProjectSummaries(portfolioId);
        // Active = not archived (status may be On track / At risk / ACTIVE, etc.).
        long active = projects.stream()
                .filter(project -> project.status() == null
                        || !"ARCHIVED".equalsIgnoreCase(project.status().trim()))
                .count();
        return new PortfolioDetailResponse(
                portfolio.getId(),
                portfolio.getWorkspace().getId(),
                portfolio.getName(),
                portfolio.getDescription(),
                portfolio.getHealthScore(),
                portfolio.getAttentionScore(),
                projects.size(),
                active,
                projects
        );
    }

    @Transactional
    public ScopeAnalyticsKpiResponse getKpis(UUID portfolioId) {
        requirePortfolio(portfolioId);
        return analyticsQueryService.getPortfolioKpis(portfolioId);
    }

    @Transactional
    public ScopeDashboardResponse getDashboard(UUID portfolioId) {
        requirePortfolio(portfolioId);
        return analyticsQueryService.getPortfolioDashboard(portfolioId);
    }

    @Transactional
    public PortfolioSummaryResponse createPortfolio(CreatePortfolioRequest request) {
        WorkspaceEntity workspace = workspaceRepository.findById(request.workspaceId())
                .orElseThrow(() -> new BusinessException(ErrorCode.WORKSPACE_001));
        String name = request.name().trim();
        if (portfolioRepository.existsByWorkspaceIdAndNameIgnoreCase(workspace.getId(), name)) {
            throw new BusinessException(ErrorCode.PORTFOLIO_002);
        }
        PortfolioEntity portfolio = portfolioRepository.save(
                new PortfolioEntity(workspace, name, blankToNull(request.description()))
        );
        addMemberships(portfolio, request.projectIds());
        return toSummary(portfolio);
    }

    @Transactional
    public PortfolioSummaryResponse updatePortfolio(UUID portfolioId, UpdatePortfolioRequest request) {
        PortfolioEntity portfolio = requirePortfolio(portfolioId);
        String name = request.name().trim();
        if (portfolioRepository.existsByWorkspaceIdAndNameIgnoreCaseAndIdNot(
                portfolio.getWorkspace().getId(),
                name,
                portfolioId
        )) {
            throw new BusinessException(ErrorCode.PORTFOLIO_002);
        }
        portfolio.setName(name);
        portfolio.setDescription(blankToNull(request.description()));
        return toSummary(portfolioRepository.save(portfolio));
    }

    @Transactional
    public void deletePortfolio(UUID portfolioId) {
        PortfolioEntity portfolio = requirePortfolio(portfolioId);
        // Memberships cascade via FK ON DELETE CASCADE; projects remain owned by workspace.
        portfolioProjectRepository.deleteByPortfolioId(portfolioId);
        portfolioRepository.delete(portfolio);
    }

    /**
     * Adds a project to a portfolio collection (idempotent). Does not remove other memberships.
     */
    @Transactional
    public PortfolioDetailResponse addProject(UUID portfolioId, AssignProjectRequest request) {
        PortfolioEntity portfolio = requirePortfolio(portfolioId);
        addMemberships(portfolio, List.of(request.projectId()));
        return getPortfolio(portfolioId);
    }

    /**
     * Bulk-add project memberships (idempotent per project). Organizational only.
     */
    @Transactional
    public PortfolioDetailResponse addProjects(UUID portfolioId, List<UUID> projectIds) {
        PortfolioEntity portfolio = requirePortfolio(portfolioId);
        addMemberships(portfolio, projectIds);
        return getPortfolio(portfolioId);
    }

    private void addMemberships(PortfolioEntity portfolio, List<UUID> projectIds) {
        if (projectIds == null || projectIds.isEmpty()) {
            return;
        }
        UUID workspaceId = portfolio.getWorkspace().getId();
        for (UUID projectId : projectIds) {
            if (projectId == null) {
                continue;
            }
            ProjectEntity project = projectRepository.findById(projectId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_001));
            if (!project.getWorkspace().getId().equals(workspaceId)) {
                throw new BusinessException(
                        ErrorCode.PORTFOLIO_003,
                        "Project and portfolio must belong to the same workspace."
                );
            }
            if (!portfolioProjectRepository.existsByPortfolioIdAndProjectId(portfolio.getId(), project.getId())) {
                portfolioProjectRepository.save(new PortfolioProjectEntity(portfolio.getId(), project.getId()));
            }
        }
    }

    /**
     * Removes a project from a portfolio collection. Project remains in the workspace.
     */
    @Transactional
    public PortfolioDetailResponse removeProject(UUID portfolioId, UUID projectId) {
        requirePortfolio(portfolioId);
        if (!projectRepository.existsById(projectId)) {
            throw new BusinessException(ErrorCode.PROJECT_001);
        }
        portfolioProjectRepository.deleteByPortfolioIdAndProjectId(portfolioId, projectId);
        return getPortfolio(portfolioId);
    }

    @Transactional(readOnly = true)
    public List<PortfolioProjectSummaryResponse> listWorkspaceProjects(UUID workspaceId) {
        if (!workspaceRepository.existsById(workspaceId)) {
            throw new BusinessException(ErrorCode.WORKSPACE_001);
        }
        return projectRepository.findByWorkspaceIdOrderByNameAsc(workspaceId).stream()
                .map(metricsService::toProjectSummary)
                .toList();
    }

    private PortfolioSummaryResponse toSummary(PortfolioEntity portfolio) {
        long total = projectRepository.countMembersByPortfolioId(portfolio.getId());
        long active = projectRepository.countActiveMembersByPortfolioId(portfolio.getId());
        return new PortfolioSummaryResponse(
                portfolio.getId(),
                portfolio.getWorkspace().getId(),
                portfolio.getName(),
                portfolio.getDescription(),
                portfolio.getHealthScore(),
                portfolio.getAttentionScore(),
                total,
                active
        );
    }

    private PortfolioEntity requirePortfolio(UUID portfolioId) {
        return portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PORTFOLIO_001));
    }

    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }


}
