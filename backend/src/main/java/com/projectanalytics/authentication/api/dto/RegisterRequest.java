package com.projectanalytics.authentication.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Public registration request (M14a). Creates a Project Analytics account only —
 * does not grant OpenProject or analytics access.
 */
public record RegisterRequest(
        @NotBlank @Email @Size(max = 255) String email,
        @NotBlank @Size(min = 8, max = 200) String password,
        /**
         * Optional username. When omitted, derived from the email local-part.
         */
        @Size(min = 3, max = 100) String username
) {
}
