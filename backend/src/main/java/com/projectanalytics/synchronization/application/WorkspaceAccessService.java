package com.projectanalytics.synchronization.application;

import com.projectanalytics.authentication.persistence.UserEntity;
import com.projectanalytics.authentication.persistence.UserRepository;
import com.projectanalytics.common.exception.BusinessException;
import com.projectanalytics.common.exception.ErrorCode;
import com.projectanalytics.portfolio.persistence.PortfolioEntity;
import com.projectanalytics.portfolio.persistence.PortfolioRepository;
import com.projectanalytics.project.persistence.ProjectEntity;
import com.projectanalytics.project.persistence.ProjectRepository;
import com.projectanalytics.synchronization.api.dto.WorkspaceMemberResponse;
import com.projectanalytics.synchronization.persistence.WorkspaceMembershipEntity;
import com.projectanalytics.synchronization.persistence.WorkspaceMembershipRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * PA workspace membership checks (Hybrid M14/M15). Backend enforcement source of truth.
 * Platform Administrator does <strong>not</strong> bypass membership for analytics.
 */
@Service
public class WorkspaceAccessService {

    private final WorkspaceMembershipRepository membershipRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final PortfolioRepository portfolioRepository;

    public WorkspaceAccessService(
            WorkspaceMembershipRepository membershipRepository,
            UserRepository userRepository,
            ProjectRepository projectRepository,
            PortfolioRepository portfolioRepository
    ) {
        this.membershipRepository = membershipRepository;
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.portfolioRepository = portfolioRepository;
    }

    @Transactional(readOnly = true)
    public List<UUID> workspaceIdsWithAnalyticsAccess(UUID userId) {
        return membershipRepository.findByUserIdAndAnalyticsAccessTrue(userId).stream()
                .map(WorkspaceMembershipEntity::getWorkspaceId)
                .toList();
    }

    @Transactional(readOnly = true)
    public Set<UUID> workspaceIdSetWithAnalyticsAccess(UUID userId) {
        return new HashSet<>(workspaceIdsWithAnalyticsAccess(userId));
    }

    @Transactional(readOnly = true)
    public void requireAnalyticsAccess(UUID workspaceId, UUID userId) {
        if (!membershipRepository.existsByWorkspaceIdAndUserIdAndAnalyticsAccessTrue(workspaceId, userId)) {
            throw new BusinessException(
                    ErrorCode.AUTH_006,
                    "You do not have analytics access to this workspace."
            );
        }
    }

    @Transactional(readOnly = true)
    public void requireWorkspaceAdmin(UUID workspaceId, UUID userId) {
        if (!membershipRepository.existsByWorkspaceIdAndUserIdAndWorkspaceAdminTrue(workspaceId, userId)) {
            throw new BusinessException(
                    ErrorCode.AUTH_006,
                    "Workspace administrator permission required for this action."
            );
        }
    }

    @Transactional(readOnly = true)
    public boolean isWorkspaceAdmin(UUID workspaceId, UUID userId) {
        return membershipRepository.existsByWorkspaceIdAndUserIdAndWorkspaceAdminTrue(workspaceId, userId);
    }

    @Transactional(readOnly = true)
    public boolean hasAnalyticsAccess(UUID workspaceId, UUID userId) {
        return membershipRepository.existsByWorkspaceIdAndUserIdAndAnalyticsAccessTrue(workspaceId, userId);
    }

    @Transactional(readOnly = true)
    public UUID requireAnalyticsAccessForProject(UUID projectId, UUID userId) {
        ProjectEntity project = projectRepository.findById(projectId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_001));
        UUID workspaceId = project.getWorkspace().getId();
        requireAnalyticsAccess(workspaceId, userId);
        return workspaceId;
    }

    @Transactional(readOnly = true)
    public UUID requireAnalyticsAccessForPortfolio(UUID portfolioId, UUID userId) {
        PortfolioEntity portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PORTFOLIO_001));
        UUID workspaceId = portfolio.getWorkspace().getId();
        requireAnalyticsAccess(workspaceId, userId);
        return workspaceId;
    }

    @Transactional(readOnly = true)
    public UUID requireWorkspaceAdminForPortfolio(UUID portfolioId, UUID userId) {
        PortfolioEntity portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PORTFOLIO_001));
        UUID workspaceId = portfolio.getWorkspace().getId();
        requireWorkspaceAdmin(workspaceId, userId);
        return workspaceId;
    }

    @Transactional
    public WorkspaceMembershipEntity grantConnectorAdmin(UUID workspaceId, UUID userId) {
        return membershipRepository.findByWorkspaceIdAndUserId(workspaceId, userId)
                .map(existing -> {
                    existing.setWorkspaceAdmin(true);
                    existing.setAnalyticsAccess(true);
                    return membershipRepository.save(existing);
                })
                .orElseGet(() -> membershipRepository.save(
                        new WorkspaceMembershipEntity(workspaceId, userId, true, true)
                ));
    }

    @Transactional
    public WorkspaceMembershipEntity grantAnalyticsAccess(UUID workspaceId, UUID userId) {
        return membershipRepository.findByWorkspaceIdAndUserId(workspaceId, userId)
                .map(existing -> {
                    existing.setAnalyticsAccess(true);
                    return membershipRepository.save(existing);
                })
                .orElseGet(() -> membershipRepository.save(
                        new WorkspaceMembershipEntity(workspaceId, userId, false, true)
                ));
    }

    /**
     * Lists memberships for a workspace. Caller must be Workspace Admin.
     */
    @Transactional(readOnly = true)
    public List<WorkspaceMemberResponse> listMembers(UUID workspaceId, UUID actorUserId) {
        requireWorkspaceAdmin(workspaceId, actorUserId);
        return membershipRepository.findByWorkspaceIdOrderByCreatedAtAsc(workspaceId).stream()
                .map(this::toMemberResponse)
                .toList();
    }

    /**
     * Grants analytics access to an existing PA user identified by email.
     * Does not promote Workspace Admin (connector-only in v1).
     */
    @Transactional
    public WorkspaceMemberResponse grantAnalyticsAccessByEmail(
            UUID workspaceId,
            UUID actorUserId,
            String email
    ) {
        requireWorkspaceAdmin(workspaceId, actorUserId);
        String normalized = email == null ? "" : email.trim();
        if (normalized.isEmpty()) {
            throw new BusinessException(ErrorCode.VALIDATION_001, "Email is required.");
        }
        UserEntity target = userRepository.findByEmailIgnoreCase(normalized)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.USER_001,
                        "No Project Analytics account found for that email. Ask them to sign up first."
                ));
        if (!target.isEnabled()) {
            throw new BusinessException(ErrorCode.AUTH_006, "That user account is disabled.");
        }
        WorkspaceMembershipEntity membership = grantAnalyticsAccess(workspaceId, target.getId());
        return toMemberResponse(membership, target);
    }

    /**
     * Revokes analytics access for a non-admin member.
     * Workspace Admins cannot be revoked via this path (reconnect/disconnect owns that lifecycle).
     */
    @Transactional
    public void revokeAnalyticsAccess(UUID workspaceId, UUID actorUserId, UUID targetUserId) {
        requireWorkspaceAdmin(workspaceId, actorUserId);
        WorkspaceMembershipEntity membership = membershipRepository
                .findByWorkspaceIdAndUserId(workspaceId, targetUserId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.USER_001,
                        "That user is not a member of this workspace."
                ));
        if (membership.isWorkspaceAdmin()) {
            throw new BusinessException(
                    ErrorCode.AUTH_006,
                    "Cannot revoke a Workspace Admin via access grants. Disconnect the connection to remove admin access."
            );
        }
        membershipRepository.delete(membership);
    }

    private WorkspaceMemberResponse toMemberResponse(WorkspaceMembershipEntity membership) {
        UserEntity user = userRepository.findById(membership.getUserId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_001));
        return toMemberResponse(membership, user);
    }

    private static WorkspaceMemberResponse toMemberResponse(
            WorkspaceMembershipEntity membership,
            UserEntity user
    ) {
        return new WorkspaceMemberResponse(
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                membership.isWorkspaceAdmin(),
                membership.isAnalyticsAccess()
        );
    }
}
