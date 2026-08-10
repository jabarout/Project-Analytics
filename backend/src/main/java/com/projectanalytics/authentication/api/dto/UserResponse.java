package com.projectanalytics.authentication.api.dto;

import java.util.UUID;

public record UserResponse(
        UUID id,
        String username,
        String email,
        String role,
        boolean enabled,
        UserPreferenceResponse preferences
) {
}
