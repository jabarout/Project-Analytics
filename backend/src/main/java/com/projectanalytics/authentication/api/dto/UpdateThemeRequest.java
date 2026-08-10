package com.projectanalytics.authentication.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateThemeRequest(
        @NotBlank @Size(max = 50) String theme
) {
}
