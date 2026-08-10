package com.projectanalytics.authentication.application;

import com.projectanalytics.authentication.api.dto.LoginRequest;
import com.projectanalytics.authentication.api.dto.LoginResponse;
import com.projectanalytics.authentication.api.dto.UserPreferenceResponse;
import com.projectanalytics.authentication.api.dto.UserResponse;
import com.projectanalytics.authentication.persistence.UserEntity;
import com.projectanalytics.authentication.persistence.UserPreferenceEntity;
import com.projectanalytics.authentication.persistence.UserPreferenceRepository;
import com.projectanalytics.authentication.persistence.UserRepository;
import com.projectanalytics.authentication.security.AuthenticatedUser;
import com.projectanalytics.authentication.security.JwtService;
import com.projectanalytics.common.exception.BusinessException;
import com.projectanalytics.common.exception.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * Authentication use cases: login, logout, current user.
 */
@Service
public class AuthenticationService {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationService.class);

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final UserPreferenceRepository userPreferenceRepository;

    public AuthenticationService(
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            UserRepository userRepository,
            UserPreferenceRepository userPreferenceRepository
    ) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.userPreferenceRepository = userPreferenceRepository;
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password())
            );
            AuthenticatedUser principal = (AuthenticatedUser) authentication.getPrincipal();
            String token = jwtService.generateToken(principal);
            Instant expiresAt = jwtService.extractExpiration(token);
            log.info("User {} authenticated successfully", principal.getUsername());
            return new LoginResponse(token, expiresAt);
        } catch (BadCredentialsException | DisabledException exception) {
            log.warn("Authentication failed for username={}", request.username());
            throw new BusinessException(ErrorCode.AUTH_001);
        }
    }

    public void logout(AuthenticatedUser user) {
        // Stateless JWT: client discards the token. Audit log only for M2.
        log.info("User {} logged out", user.getUsername());
    }

    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(UUID userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_001));
        UserPreferenceEntity preferences = userPreferenceRepository.findByUserId(userId)
                .orElseGet(() -> new UserPreferenceEntity(user));
        return toUserResponse(user, preferences);
    }

    public static UserResponse toUserResponse(UserEntity user, UserPreferenceEntity preferences) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole().name(),
                user.isEnabled(),
                new UserPreferenceResponse(
                        preferences.getTheme(),
                        preferences.getLanguage(),
                        preferences.getDashboardConfiguration()
                )
        );
    }
}
