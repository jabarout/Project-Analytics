package com.projectanalytics.authentication.application;

import com.projectanalytics.authentication.api.dto.LoginRequest;
import com.projectanalytics.authentication.api.dto.LoginResponse;
import com.projectanalytics.authentication.api.dto.RegisterRequest;
import com.projectanalytics.authentication.api.dto.UserPreferenceResponse;
import com.projectanalytics.authentication.api.dto.UserResponse;
import com.projectanalytics.authentication.config.RegistrationProperties;
import com.projectanalytics.authentication.domain.Role;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Authentication use cases: register, login, logout, current user.
 */
@Service
public class AuthenticationService {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationService.class);
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9._-]{3,100}$");

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final UserPreferenceRepository userPreferenceRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordPolicy passwordPolicy;
    private final RegistrationProperties registrationProperties;
    private final EmailConfirmationService emailConfirmationService;

    public AuthenticationService(
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            UserRepository userRepository,
            UserPreferenceRepository userPreferenceRepository,
            PasswordEncoder passwordEncoder,
            PasswordPolicy passwordPolicy,
            RegistrationProperties registrationProperties,
            EmailConfirmationService emailConfirmationService
    ) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.userPreferenceRepository = userPreferenceRepository;
        this.passwordEncoder = passwordEncoder;
        this.passwordPolicy = passwordPolicy;
        this.registrationProperties = registrationProperties;
        this.emailConfirmationService = emailConfirmationService;
    }

    /**
     * Creates a Project Analytics account (VIEWER). Never promotes to platform admin.
     * Does not grant OpenProject connection or analytics access.
     * Does not return a JWT — email must be confirmed before login.
     */
    @Transactional
    public java.util.Map<String, String> register(RegisterRequest request) {
        if (!registrationProperties.isEnabled()) {
            throw new BusinessException(ErrorCode.USER_006);
        }

        String email = normalizeEmail(request.email());
        if (email == null) {
            throw new BusinessException(ErrorCode.USER_007);
        }
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new BusinessException(ErrorCode.USER_002);
        }

        String username = resolveUsername(request.username(), email);

        passwordPolicy.validate(request.password());

        UserEntity user = new UserEntity(
                username,
                email,
                passwordEncoder.encode(request.password()),
                Role.VIEWER
        );
        user.setEmailVerified(false);
        // Flush before confirmation token insert — token row FKs users(id).
        user = userRepository.saveAndFlush(user);
        userPreferenceRepository.save(new UserPreferenceEntity(user));
        emailConfirmationService.issueConfirmation(user);
        log.info(
                "Registered Project Analytics user id={} username={} (awaiting email confirmation; no analytics access yet)",
                user.getId(),
                username
        );

        return java.util.Map.of(
                "message",
                "Account created. Please confirm your email before signing in.",
                "email",
                email
        );
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password())
            );
            AuthenticatedUser principal = (AuthenticatedUser) authentication.getPrincipal();
            UserEntity user = userRepository.findById(principal.getId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.USER_001));
            if (!user.isEmailVerified()) {
                throw new BusinessException(
                        ErrorCode.AUTH_008,
                        "Please confirm your email before signing in. Check your inbox for the confirmation link."
                );
            }
            String token = jwtService.generateToken(principal);
            Instant expiresAt = jwtService.extractExpiration(token);
            log.info("User {} authenticated successfully", principal.getUsername());
            return new LoginResponse(token, expiresAt);
        } catch (BadCredentialsException | DisabledException exception) {
            log.warn("Authentication failed for username={}", request.username());
            throw new BusinessException(ErrorCode.AUTH_001);
        }
    }

    private static String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String resolveUsername(String requested, String email) {
        boolean explicit = requested != null && !requested.isBlank();
        String username;
        if (explicit) {
            username = requested.trim();
        } else {
            String local = email.substring(0, email.indexOf('@'));
            username = local.replaceAll("[^a-zA-Z0-9._-]", "");
            if (username.length() < 3) {
                username = "user" + UUID.randomUUID().toString().substring(0, 8);
            }
        }
        if (!USERNAME_PATTERN.matcher(username).matches()) {
            throw new BusinessException(
                    ErrorCode.VALIDATION_003,
                    "Username must be 3–100 characters and use only letters, digits, dot, underscore, or hyphen."
            );
        }
        if (explicit) {
            if (userRepository.existsByUsernameIgnoreCase(username)) {
                throw new BusinessException(ErrorCode.USER_003);
            }
            return username;
        }
        // Derived from email: suffix on collision.
        String candidate = username;
        int suffix = 1;
        while (userRepository.existsByUsernameIgnoreCase(candidate)) {
            candidate = username + suffix;
            suffix++;
            if (suffix > 1000) {
                throw new BusinessException(ErrorCode.USER_003);
            }
        }
        return candidate;
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
