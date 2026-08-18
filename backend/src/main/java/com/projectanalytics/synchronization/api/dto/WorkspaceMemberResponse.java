package com.projectanalytics.synchronization.api.dto;

import java.util.UUID;

/**
 * PA workspace membership row for access-grant UI (M15).
 */
public record WorkspaceMemberResponse(
        UUID userId,
        String email,
        String username,
        boolean workspaceAdmin,
        boolean analyticsAccess
) {
}
