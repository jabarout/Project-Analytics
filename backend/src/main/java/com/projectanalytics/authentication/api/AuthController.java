package com.projectanalytics.authentication.api;

import com.projectanalytics.authentication.api.dto.ForgotPasswordRequest;
import com.projectanalytics.authentication.api.dto.LoginRequest;
import com.projectanalytics.authentication.api.dto.LoginResponse;
import com.projectanalytics.authentication.api.dto.RegisterRequest;
import com.projectanalytics.authentication.api.dto.ResetPasswordRequest;
import com.projectanalytics.authentication.api.dto.UserResponse;
import com.projectanalytics.authentication.application.AuthenticationService;
import com.projectanalytics.authentication.application.PasswordResetService;
import com.projectanalytics.authentication.security.AuthenticatedUser;
import com.projectanalytics.common.api.ApiResponse;
import com.projectanalytics.common.constants.ApplicationConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Authentication endpoints (API Specification §6).
 */
@RestController
@RequestMapping(ApplicationConstants.API_V1_BASE_PATH + "/auth")
@Tag(name = "Authentication", description = "Login, logout, and current user")
public class AuthController {

    private final AuthenticationService authenticationService;
    private final PasswordResetService passwordResetService;

    public AuthController(
            AuthenticationService authenticationService,
            PasswordResetService passwordResetService
    ) {
        this.authenticationService = authenticationService;
        this.passwordResetService = passwordResetService;
    }

    @PostMapping("/register")
    @Operation(
            summary = "Register",
            description = "Creates a Project Analytics account (email/password). Does not grant OpenProject or analytics access."
    )
    public ApiResponse<LoginResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ApiResponse.of(authenticationService.register(request));
    }

    @PostMapping("/login")
    @Operation(summary = "Login", description = "Authenticates with username or email and returns a JWT access token.")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.of(authenticationService.login(request));
    }

    @PostMapping("/forgot-password")
    @Operation(
            summary = "Forgot password",
            description = "Always returns a generic success message. Never reveals whether the email exists."
    )
    public ApiResponse<java.util.Map<String, String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        passwordResetService.forgotPassword(request);
        return ApiResponse.of(java.util.Map.of(
                "message",
                "If an account exists for that email, password reset instructions have been sent."
        ));
    }

    @PostMapping("/reset-password")
    @Operation(
            summary = "Reset password",
            description = "Consumes a single-use reset token and sets a new password (password policy enforced)."
    )
    public ApiResponse<java.util.Map<String, String>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        passwordResetService.resetPassword(request);
        return ApiResponse.of(java.util.Map.of("message", "Password updated. You can sign in with your new password."));
    }

    @PostMapping("/logout")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Logout", description = "Records logout for the authenticated user. Client must discard the JWT.")
    public ApiResponse<Void> logout(@AuthenticationPrincipal AuthenticatedUser user) {
        authenticationService.logout(user);
        return ApiResponse.of(null);
    }

    @GetMapping("/me")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Current user", description = "Returns the authenticated user profile and preferences.")
    public ApiResponse<UserResponse> me(@AuthenticationPrincipal AuthenticatedUser user) {
        return ApiResponse.of(authenticationService.getCurrentUser(user.getId()));
    }
}
