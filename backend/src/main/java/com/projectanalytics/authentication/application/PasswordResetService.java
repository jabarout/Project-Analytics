package com.projectanalytics.authentication.application;

import com.projectanalytics.authentication.api.dto.ForgotPasswordRequest;
import com.projectanalytics.authentication.api.dto.ResetPasswordRequest;
import com.projectanalytics.authentication.persistence.PasswordResetTokenEntity;
import com.projectanalytics.authentication.persistence.PasswordResetTokenRepository;
import com.projectanalytics.authentication.persistence.UserEntity;
import com.projectanalytics.authentication.persistence.UserRepository;
import com.projectanalytics.common.exception.BusinessException;
import com.projectanalytics.common.exception.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Arrays;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

/**
 * Secure forgot/reset password flow (Phase 4).
 * Always returns a generic response for forgot-password (no account enumeration).
 * Raw tokens are never stored; only SHA-256 hashes.
 */
@Service
public class PasswordResetService {

    private static final Logger log = LoggerFactory.getLogger(PasswordResetService.class);
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordPolicy passwordPolicy;
    private final ObjectProvider<JavaMailSender> mailSender;
    private final Environment environment;
    private final long tokenTtlMinutes;
    private final String frontendBaseUrl;
    private final String mailFrom;
    private final boolean mailEnabled;

    public PasswordResetService(
            UserRepository userRepository,
            PasswordResetTokenRepository tokenRepository,
            PasswordEncoder passwordEncoder,
            PasswordPolicy passwordPolicy,
            ObjectProvider<JavaMailSender> mailSender,
            Environment environment,
            @Value("${projectanalytics.security.password-reset.ttl-minutes:60}") long tokenTtlMinutes,
            @Value("${projectanalytics.security.password-reset.frontend-base-url:http://localhost:4200}")
            String frontendBaseUrl,
            @Value("${projectanalytics.security.password-reset.mail-from:noreply@projectanalytics.local}")
            String mailFrom,
            @Value("${projectanalytics.security.password-reset.mail-enabled:false}") boolean mailEnabled
    ) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.passwordPolicy = passwordPolicy;
        this.mailSender = mailSender;
        this.environment = environment;
        this.tokenTtlMinutes = tokenTtlMinutes;
        this.frontendBaseUrl = frontendBaseUrl.endsWith("/")
                ? frontendBaseUrl.substring(0, frontendBaseUrl.length() - 1)
                : frontendBaseUrl;
        this.mailFrom = mailFrom;
        this.mailEnabled = mailEnabled;
    }

    /**
     * Always succeeds with the same client message. Never reveals whether the email exists.
     */
    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        String email = request.email() == null ? "" : request.email().trim().toLowerCase(Locale.ROOT);
        Optional<UserEntity> userOpt = email.isBlank()
                ? Optional.empty()
                : userRepository.findByEmailIgnoreCase(email);

        if (userOpt.isPresent() && userOpt.get().isEnabled()) {
            UserEntity user = userOpt.get();
            tokenRepository.deleteUnusedByUserId(user.getId());
            String rawToken = generateRawToken();
            String hash = sha256Hex(rawToken);
            Instant expiresAt = Instant.now().plusSeconds(tokenTtlMinutes * 60);
            tokenRepository.save(new PasswordResetTokenEntity(user.getId(), hash, expiresAt));
            deliverResetLink(user.getEmail(), rawToken);
        } else {
            // Constant-ish work: still hash a dummy token so timing is closer.
            sha256Hex(generateRawToken());
        }
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        passwordPolicy.validate(request.newPassword());
        String hash = sha256Hex(request.token().trim());
        PasswordResetTokenEntity token = tokenRepository.findByTokenHash(hash)
                .orElseThrow(() -> new BusinessException(ErrorCode.AUTH_003, "Invalid or expired reset token."));

        Instant now = Instant.now();
        if (token.isUsed()) {
            throw new BusinessException(ErrorCode.AUTH_003, "Invalid or expired reset token.");
        }
        if (token.isExpired(now)) {
            throw new BusinessException(ErrorCode.AUTH_003, "Invalid or expired reset token.");
        }

        UserEntity user = userRepository.findById(token.getUserId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_001));
        if (!user.isEnabled()) {
            throw new BusinessException(ErrorCode.AUTH_005, "Account is disabled.");
        }

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        user.incrementCredentialsVersion();
        userRepository.save(user);

        token.setUsedAt(now);
        tokenRepository.save(token);
        tokenRepository.deleteUnusedByUserId(user.getId());
        log.info("Password reset completed for userId={} (credentialsVersion={})", user.getId(), user.getCredentialsVersion());
    }

    private void deliverResetLink(String email, String rawToken) {
        String link = frontendBaseUrl + "/login/reset-password?token=" + rawToken;
        JavaMailSender sender = mailSender.getIfAvailable();
        if (mailEnabled && sender != null) {
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(email);
                message.setFrom(mailFrom);
                message.setSubject("Reset your Project Analytics password");
                message.setText(
                        "Use this link to reset your password (expires in "
                                + tokenTtlMinutes
                                + " minutes):\n\n"
                                + link
                                + "\n\nIf you did not request this, you can ignore this email."
                );
                sender.send(message);
                log.info("Password reset email queued for user email domain={}", emailDomain(email));
                return;
            } catch (Exception exception) {
                log.error("Failed to send password reset email domain={}", emailDomain(email), exception);
                // Still do not reveal failure to client on forgot endpoint.
            }
        }

        // Local/dev only: never print raw tokens in production logs.
        if (isLocalProfile()) {
            log.warn(
                    "Password reset mail disabled/unavailable — DEV reset link for domain={}: {}",
                    emailDomain(email),
                    link
            );
        } else {
            log.error(
                    "Password reset token created but mail is not configured (domain={}). "
                            + "Set projectanalytics.security.password-reset.mail-enabled=true and spring.mail.*",
                    emailDomain(email)
            );
        }
    }

    private boolean isLocalProfile() {
        return Arrays.stream(environment.getActiveProfiles())
                .anyMatch(p -> p.equalsIgnoreCase("dev") || p.equalsIgnoreCase("test") || p.equalsIgnoreCase("local"));
    }

    private static String emailDomain(String email) {
        int at = email.indexOf('@');
        return at >= 0 ? email.substring(at + 1) : "(unknown)";
    }

    private static String generateRawToken() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /**
     * Test helper: creates a valid unused reset token and returns the raw token value.
     * Not for production HTTP use.
     */
    @Transactional
    public String issueRawTokenForTests(UUID userId) {
        String rawToken = generateRawToken();
        String hash = sha256Hex(rawToken);
        // Long TTL avoids H2 timestamp TZ edge cases in tests.
        Instant expiresAt = Instant.now().plusSeconds(7 * 24 * 3600);
        tokenRepository.saveAndFlush(new PasswordResetTokenEntity(userId, hash, expiresAt));
        PasswordResetTokenEntity stored = tokenRepository.findByTokenHash(hash)
                .orElseThrow(() -> new IllegalStateException("Failed to persist password reset token for tests"));
        if (stored.isExpired(Instant.now())) {
            throw new IllegalStateException(
                    "Persisted reset token appears expired immediately (expiresAt=" + stored.getExpiresAt() + ")"
            );
        }
        return rawToken;
    }

    public static String sha256Hex(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashed);
        } catch (Exception exception) {
            throw new IllegalStateException("SHA-256 unavailable", exception);
        }
    }
}
