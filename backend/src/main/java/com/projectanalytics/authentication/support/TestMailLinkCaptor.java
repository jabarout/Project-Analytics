package com.projectanalytics.authentication.support;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Test-only capture of raw email tokens when mail delivery is disabled / logged.
 */
@Component
@Profile("test")
public class TestMailLinkCaptor {

    private final AtomicReference<String> lastConfirmationToken = new AtomicReference<>();
    private final AtomicReference<String> lastPasswordResetToken = new AtomicReference<>();

    public void captureConfirmationToken(String rawToken) {
        lastConfirmationToken.set(rawToken);
    }

    public void capturePasswordResetToken(String rawToken) {
        lastPasswordResetToken.set(rawToken);
    }

    public String lastConfirmationToken() {
        return lastConfirmationToken.get();
    }

    public String lastPasswordResetToken() {
        return lastPasswordResetToken.get();
    }

    public void clear() {
        lastConfirmationToken.set(null);
        lastPasswordResetToken.set(null);
    }
}
