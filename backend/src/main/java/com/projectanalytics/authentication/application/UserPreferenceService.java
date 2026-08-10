package com.projectanalytics.authentication.application;

import com.projectanalytics.authentication.api.dto.UpdatePreferencesRequest;
import com.projectanalytics.authentication.api.dto.UpdateThemeRequest;
import com.projectanalytics.authentication.api.dto.UserPreferenceResponse;
import com.projectanalytics.authentication.api.dto.UserResponse;
import com.projectanalytics.authentication.domain.Theme;
import com.projectanalytics.authentication.persistence.UserEntity;
import com.projectanalytics.authentication.persistence.UserPreferenceEntity;
import com.projectanalytics.authentication.persistence.UserPreferenceRepository;
import com.projectanalytics.authentication.persistence.UserRepository;
import com.projectanalytics.common.exception.BusinessException;
import com.projectanalytics.common.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/**
 * User preference management use cases.
 */
@Service
public class UserPreferenceService {

    private static final Set<String> SUPPORTED_LANGUAGES = Set.of("en", "fr", "de", "es");

    private final UserRepository userRepository;
    private final UserPreferenceRepository userPreferenceRepository;

    public UserPreferenceService(
            UserRepository userRepository,
            UserPreferenceRepository userPreferenceRepository
    ) {
        this.userRepository = userRepository;
        this.userPreferenceRepository = userPreferenceRepository;
    }

    @Transactional
    public UserPreferenceResponse updatePreferences(UUID userId, UpdatePreferencesRequest request) {
        UserPreferenceEntity preferences = getOrCreatePreferences(userId);
        preferences.setTheme(normalizeTheme(request.theme()));
        preferences.setLanguage(normalizeLanguage(request.language()));
        preferences.setDashboardConfiguration(request.dashboardConfiguration());
        userPreferenceRepository.save(preferences);
        return toResponse(preferences);
    }

    @Transactional
    public UserPreferenceResponse updateTheme(UUID userId, UpdateThemeRequest request) {
        UserPreferenceEntity preferences = getOrCreatePreferences(userId);
        preferences.setTheme(normalizeTheme(request.theme()));
        userPreferenceRepository.save(preferences);
        return toResponse(preferences);
    }

    @Transactional(readOnly = true)
    public UserResponse getCurrentUserProfile(UUID userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_001));
        UserPreferenceEntity preferences = getOrCreatePreferencesReadOnly(userId, user);
        return AuthenticationService.toUserResponse(user, preferences);
    }

    private UserPreferenceEntity getOrCreatePreferences(UUID userId) {
        return userPreferenceRepository.findByUserId(userId)
                .orElseGet(() -> {
                    UserEntity user = userRepository.findById(userId)
                            .orElseThrow(() -> new BusinessException(ErrorCode.USER_001));
                    return userPreferenceRepository.save(new UserPreferenceEntity(user));
                });
    }

    private UserPreferenceEntity getOrCreatePreferencesReadOnly(UUID userId, UserEntity user) {
        return userPreferenceRepository.findByUserId(userId)
                .orElseGet(() -> new UserPreferenceEntity(user));
    }

    private String normalizeTheme(String theme) {
        try {
            return Theme.fromConfigValue(theme).toConfigValue();
        } catch (IllegalArgumentException exception) {
            throw new BusinessException(ErrorCode.USER_005, "Unsupported theme value.");
        }
    }

    private String normalizeLanguage(String language) {
        if (language == null || language.isBlank()) {
            throw new BusinessException(ErrorCode.USER_005, "Language is required.");
        }
        String normalized = language.trim().toLowerCase(Locale.ROOT);
        if (!SUPPORTED_LANGUAGES.contains(normalized)) {
            throw new BusinessException(ErrorCode.USER_005, "Unsupported language value.");
        }
        return normalized;
    }

    private UserPreferenceResponse toResponse(UserPreferenceEntity preferences) {
        return new UserPreferenceResponse(
                preferences.getTheme(),
                preferences.getLanguage(),
                preferences.getDashboardConfiguration()
        );
    }
}
