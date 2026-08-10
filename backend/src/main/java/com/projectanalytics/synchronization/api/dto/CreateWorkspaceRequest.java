package com.projectanalytics.synchronization.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateWorkspaceRequest(
        @NotBlank @Size(max = 200) String name,
        @Size(max = 500) String baseUrl
) {
}
