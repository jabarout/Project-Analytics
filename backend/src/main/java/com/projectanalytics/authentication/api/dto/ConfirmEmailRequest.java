package com.projectanalytics.authentication.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ConfirmEmailRequest(
        @NotBlank @Size(max = 500) String token
) {
}
