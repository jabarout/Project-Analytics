package com.projectanalytics.authentication.api;

import com.projectanalytics.authentication.api.dto.UpdatePreferencesRequest;
import com.projectanalytics.authentication.api.dto.UpdateThemeRequest;
import com.projectanalytics.authentication.api.dto.UserPreferenceResponse;
import com.projectanalytics.authentication.api.dto.UserResponse;
import com.projectanalytics.authentication.application.UserPreferenceService;
import com.projectanalytics.authentication.security.AuthenticatedUser;
import com.projectanalytics.common.api.ApiResponse;
import com.projectanalytics.common.constants.ApplicationConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * User profile and preference endpoints (API Specification §15).
 */
@RestController
@RequestMapping(ApplicationConstants.API_V1_BASE_PATH + "/users")
@Tag(name = "Users", description = "Current user profile and preferences")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserPreferenceService userPreferenceService;

    public UserController(UserPreferenceService userPreferenceService) {
        this.userPreferenceService = userPreferenceService;
    }

    @GetMapping("/me")
    @Operation(summary = "Current user profile")
    public ApiResponse<UserResponse> getMe(@AuthenticationPrincipal AuthenticatedUser user) {
        return ApiResponse.of(userPreferenceService.getCurrentUserProfile(user.getId()));
    }

    @PutMapping("/me/preferences")
    @Operation(summary = "Update preferences")
    public ApiResponse<UserPreferenceResponse> updatePreferences(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody UpdatePreferencesRequest request
    ) {
        return ApiResponse.of(userPreferenceService.updatePreferences(user.getId(), request));
    }

    @PatchMapping("/me/theme")
    @Operation(summary = "Update theme")
    public ApiResponse<UserPreferenceResponse> updateTheme(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody UpdateThemeRequest request
    ) {
        return ApiResponse.of(userPreferenceService.updateTheme(user.getId(), request));
    }
}
