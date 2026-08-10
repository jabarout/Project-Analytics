package com.projectanalytics.infrastructure.openproject;

import com.projectanalytics.common.exception.BusinessException;
import com.projectanalytics.common.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EnvironmentApiKeyOpenProjectCredentialResolverTest {

    private OpenProjectProperties properties;
    private EnvironmentApiKeyOpenProjectCredentialResolver resolver;

    @BeforeEach
    void setUp() {
        properties = new OpenProjectProperties();
        properties.setApiKey("test-secret-key");
        properties.setTimeoutSeconds(15);
        properties.setVerifySsl(true);
        resolver = new EnvironmentApiKeyOpenProjectCredentialResolver(properties);
    }

    @Test
    void resolve_usesEnvironmentApiKeyAndWorkspaceBaseUrl() {
        UUID workspaceId = UUID.randomUUID();

        OpenProjectConnectionProperties connection =
                resolver.resolve(workspaceId, "https://openproject.example.com/");

        assertThat(connection.baseUrl()).isEqualTo("https://openproject.example.com");
        assertThat(connection.timeoutSeconds()).isEqualTo(15);
        assertThat(connection.verifySsl()).isTrue();
        assertThat(connection.credentials().scheme()).isEqualTo(OpenProjectAuthScheme.API_KEY);
        assertThat(connection.credentials().apiKey()).isEqualTo("test-secret-key");
        assertThat(connection.credentials().accessToken()).isNull();
    }

    @Test
    void resolve_requiresApiKey() {
        properties.setApiKey(" ");

        assertThatThrownBy(() -> resolver.resolve(UUID.randomUUID(), "https://openproject.example.com"))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.SYNC_005);
    }
}
