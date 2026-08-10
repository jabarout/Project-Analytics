package com.projectanalytics.authentication.api.dto;

public record UserPreferenceResponse(
        String theme,
        String language,
        String dashboardConfiguration
) {
}
