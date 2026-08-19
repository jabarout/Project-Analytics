package com.projectanalytics.authentication.application;

import com.projectanalytics.authentication.api.dto.ConfirmEmailRequest;
import com.projectanalytics.authentication.api.dto.ResendConfirmationRequest;
import com.projectanalytics.authentication.persistence.EmailConfirmationTokenEntity;
import com.projectanalytics.authentication.persistence.EmailConfirmationTokenRepository;
import com.projectanalytics.authentication.persistence.UserEntity;
import com.projectanalytics.authentication.persistence.UserRepository;
import com.projectanalytics.authentication.support.TestMailLinkCaptor;
import com.projectanalytics.common.exception.BusinessException;
import com.projectanalytics.common.exception.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
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
 * Signup email confirmation. Accounts cannot sign in until verified.
 */
@Service
public class EmailConfirmationService {

    private static final Logger log = LoggerFactory.getLogger(EmailConfirmationService.class);
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final EmailConfirmationTokenRepository tokenRepository;
    private final ObjectProvider<JavaMailSender> mailSender;
    private final ObjectProvider<TestMailLinkCaptor> testMailLinkCaptor;
    private final Environment environment;
    private final long tokenTtlMinutes;
    private final String frontendBaseUrl;
    private final String mailFrom;
    private final boolean mailEnabled;

    public EmailConfirmationService(
            UserRepository userRepository,
            EmailConfirmationTokenRepository tokenRepository,
            ObjectProvider<JavaMailSender> mailSender,
            ObjectProvider<TestMailLinkCaptor> testMailLinkCaptor,
            Environment environment,
            @Value("${projectanalytics.security.email-confirmation.ttl-minutes:1440}") long tokenTtlMinutes,
            @Value("${projectanalytics.security.password-reset.frontend-base-url:http://localhost:4200}")
            String frontendBaseUrl,
            @Value("${projectanalytics.security.password-reset.mail-from:noreply@projectanalytics.local}")
            String mailFrom,
            @Value("${projectanalytics.security.password-reset.mail-enabled:false}") boolean mailEnabled
    ) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.mailSender = mailSender;
        this.testMailLinkCaptor = testMailLinkCaptor;
        this.environment = environment;
        this.tokenTtlMinutes = tokenTtlMinutes;
        this.frontendBaseUrl = frontendBaseUrl.endsWith("/")
                ? frontendBaseUrl.substring(0, frontendBaseUrl.length() - 1)
                : frontendBaseUrl;
        this.mailFrom = mailFrom;
        this.mailEnabled = mailEnabled;
    }

    @Transactional
    public void issueConfirmation(UserEntity user) {
        tokenRepository.deleteUnusedByUserId(user.getId());
        String rawToken = generateRawToken();
        Instant expiresAt = Instant.now().plusSeconds(Math.max(60, tokenTtlMinutes) * 60);
        tokenRepository.save(new EmailConfirmationTokenEntity(user.getId(), sha256Hex(rawToken), expiresAt));
        deliverConfirmationLink(user.getEmail(), rawToken);
    }

    /**
     * Always returns a generic client message (no account enumeration).
     */
    @Transactional
    public void resendConfirmation(ResendConfirmationRequest request) {
        String email = request.email() == null ? "" : request.email().trim().toLowerCase(Locale.ROOT);
        Optional<UserEntity> userOpt = email.isBlank()
                ? Optional.empty()
                : userRepository.findByEmailIgnoreCase(email);
        if (userOpt.isPresent()) {
            UserEntity user = userOpt.get();
            if (user.isEnabled() && !user.isEmailVerified()) {
                issueConfirmation(user);
                return;
            }
        }
        sha256Hex(generateRawToken());
    }

    @Transactional
    public void confirmEmail(ConfirmEmailRequest request) {
        String hash = sha256Hex(request.token().trim());
        EmailConfirmationTokenEntity token = tokenRepository.findByTokenHash(hash)
                .orElseThrow(() -> new BusinessException(ErrorCode.AUTH_003, "Invalid or expired confirmation token."));
        Instant now = Instant.now();
        if (token.isUsed() || token.isExpired(now)) {
            throw new BusinessException(ErrorCode.AUTH_003, "Invalid or expired confirmation token.");
        }
        UserEntity user = userRepository.findById(token.getUserId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_001));
        user.setEmailVerified(true);
        userRepository.save(user);
        token.setUsedAt(now);
        tokenRepository.save(token);
        tokenRepository.deleteUnusedByUserId(user.getId());
        log.info("Email confirmed for user id={} email={}", user.getId(), user.getEmail());
    }

    private void deliverConfirmationLink(String email, String rawToken) {
        String link = frontendBaseUrl + "/login/confirm-email?token=" + rawToken;
        TestMailLinkCaptor captor = testMailLinkCaptor.getIfAvailable();
        if (captor != null) {
            captor.captureConfirmationToken(rawToken);
        }
        if (!mailEnabled) {
            if (isLocalProfile()) {
                log.info("Email confirmation link for {} (mail disabled): {}", email, link);
            } else {
                log.warn(
                        "Email confirmation mail is disabled; confirmation email for {} was not sent. "
                                + "Set PASSWORD_RESET_MAIL_ENABLED=true and spring.mail.* for production.",
                        email
                );
            }
            return;
        }
        JavaMailSender sender = mailSender.getIfAvailable();
        if (sender == null) {
            throw new BusinessException(
                    ErrorCode.SYSTEM_005,
                    "Mail is enabled but JavaMailSender is not configured. Set spring.mail.host."
            );
        }
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mailFrom);
        message.setTo(email);
        message.setSubject("Confirm your Project Analytics account");
        message.setText(
                "Welcome to Project Analytics.\n\n"
                        + "Confirm your email by opening this link:\n"
                        + link + "\n\n"
                        + "If you did not create an account, you can ignore this message.\n"
        );
        sender.send(message);
    }

    private boolean isLocalProfile() {
        return Arrays.stream(environment.getActiveProfiles())
                .anyMatch(p -> p.equalsIgnoreCase("dev") || p.equalsIgnoreCase("test") || p.equalsIgnoreCase("local"));
    }

    private static String generateRawToken() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static String sha256Hex(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashed);
        } catch (Exception exception) {
            throw new IllegalStateException("SHA-256 not available", exception);
        }
    }
}
