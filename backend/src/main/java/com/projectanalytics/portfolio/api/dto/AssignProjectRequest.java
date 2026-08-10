package com.projectanalytics.portfolio.api.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AssignProjectRequest(
        @NotNull UUID projectId
) {
}
