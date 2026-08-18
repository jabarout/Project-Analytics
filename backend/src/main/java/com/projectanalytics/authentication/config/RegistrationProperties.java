package com.projectanalytics.authentication.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Controls whether public self-signup is allowed (M14a).
 * Can be disabled for invite-only / locked-down deploys without removing the endpoint surface.
 */
@ConfigurationProperties(prefix = "projectanalytics.security.registration")
public class RegistrationProperties {

    /**
     * When false, {@code POST /auth/register} returns USER_006.
     */
    private boolean enabled = true;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
