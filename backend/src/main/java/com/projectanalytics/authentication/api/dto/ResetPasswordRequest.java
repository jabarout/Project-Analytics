package com.projectanalytics.authentication.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(
        @NotBlank @Size(min = 20, max = 200) String token,
        @NotBlank @Size(min = 8, max = 200) String newPassword
) {
}
