package com.projectanalytics.authentication.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdatePreferencesRequest(
        @NotBlank @Size(max = 50) String theme,
        @NotBlank @Size(max = 20) String language,
        @Size(max = 10000) String dashboardConfiguration
) {
}
