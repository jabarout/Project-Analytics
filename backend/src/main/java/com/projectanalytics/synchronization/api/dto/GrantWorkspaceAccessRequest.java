package com.projectanalytics.synchronization.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Grant analytics access to an existing Project Analytics user (by email).
 * Does not promote Workspace Admin — that remains connector-only in v1.
 */
public record GrantWorkspaceAccessRequest(
        @NotBlank @Email @Size(max = 255) String email
) {
}
