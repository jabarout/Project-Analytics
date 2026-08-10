package com.projectanalytics.infrastructure.openproject;

import com.projectanalytics.common.exception.BusinessException;
import com.projectanalytics.common.exception.ErrorCode;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RestOpenProjectClientTest {

    @Test
    void parseIsoDurationHours_convertsHoursAndMinutes() {
        assertThat(RestOpenProjectClient.parseIsoDurationHours("PT8H")).isEqualByComparingTo("8.00");
        assertThat(RestOpenProjectClient.parseIsoDurationHours("PT1H30M"))
                .isEqualByComparingTo(new BigDecimal("1.50"));
        assertThat(RestOpenProjectClient.parseIsoDurationHours(null)).isNull();
    }

    @Test
    void buildAuthorizationHeader_usesBasicAuthForApiKey() {
        String header = RestOpenProjectClient.buildAuthorizationHeader(
                OpenProjectCredentials.ofApiKey("my-api-key")
        );

        String expected = "Basic " + Base64.getEncoder()
                .encodeToString("apikey:my-api-key".getBytes(StandardCharsets.UTF_8));
        assertThat(header).isEqualTo(expected);
    }

    @Test
    void buildAuthorizationHeader_usesBearerForAccessToken() {
        String header = RestOpenProjectClient.buildAuthorizationHeader(
                OpenProjectCredentials.ofBearerToken("oauth-access-token")
        );

        assertThat(header).isEqualTo("Bearer oauth-access-token");
    }

    @Test
    void buildAuthorizationHeader_rejectsMissingApiKey() {
        assertThatThrownBy(() -> RestOpenProjectClient.buildAuthorizationHeader(
                OpenProjectCredentials.ofApiKey(" ")
        ))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.SYNC_005);
    }
}
