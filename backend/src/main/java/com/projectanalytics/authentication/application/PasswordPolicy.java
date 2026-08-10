package com.projectanalytics.authentication.application;

import com.projectanalytics.common.exception.BusinessException;
import com.projectanalytics.common.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Configurable password policy (Security documentation).
 */
@Component
public class PasswordPolicy {

    private final int minLength;
    private final boolean requireUppercase;
    private final boolean requireLowercase;
    private final boolean requireDigit;
    private final boolean requireSpecial;

    private static final Pattern UPPER = Pattern.compile("[A-Z]");
    private static final Pattern LOWER = Pattern.compile("[a-z]");
    private static final Pattern DIGIT = Pattern.compile("[0-9]");
    private static final Pattern SPECIAL = Pattern.compile("[^A-Za-z0-9]");

    public PasswordPolicy(
            @Value("${projectanalytics.security.password.min-length:8}") int minLength,
            @Value("${projectanalytics.security.password.require-uppercase:true}") boolean requireUppercase,
            @Value("${projectanalytics.security.password.require-lowercase:true}") boolean requireLowercase,
            @Value("${projectanalytics.security.password.require-digit:true}") boolean requireDigit,
            @Value("${projectanalytics.security.password.require-special:true}") boolean requireSpecial
    ) {
        this.minLength = minLength;
        this.requireUppercase = requireUppercase;
        this.requireLowercase = requireLowercase;
        this.requireDigit = requireDigit;
        this.requireSpecial = requireSpecial;
    }

    public void validate(String password) {
        if (password == null || password.isBlank()) {
            throw new BusinessException(ErrorCode.USER_004, "Password is required.");
        }

        List<String> violations = new ArrayList<>();
        if (password.length() < minLength) {
            violations.add("Password must be at least " + minLength + " characters.");
        }
        if (requireUppercase && !UPPER.matcher(password).find()) {
            violations.add("Password must contain an uppercase letter.");
        }
        if (requireLowercase && !LOWER.matcher(password).find()) {
            violations.add("Password must contain a lowercase letter.");
        }
        if (requireDigit && !DIGIT.matcher(password).find()) {
            violations.add("Password must contain a number.");
        }
        if (requireSpecial && !SPECIAL.matcher(password).find()) {
            violations.add("Password must contain a special character.");
        }

        if (!violations.isEmpty()) {
            throw new BusinessException(ErrorCode.USER_004, ErrorCode.USER_004.getDefaultMessage(), violations);
        }
    }
}
