package com.projectanalytics.authentication;

import com.projectanalytics.authentication.application.PasswordPolicy;
import com.projectanalytics.common.exception.BusinessException;
import com.projectanalytics.common.exception.ErrorCode;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PasswordPolicyTest {

    private final PasswordPolicy passwordPolicy = new PasswordPolicy(8, true, true, true, true);

    @Test
    void acceptsStrongPassword() {
        assertThatCode(() -> passwordPolicy.validate("Admin123!")).doesNotThrowAnyException();
    }

    @Test
    void rejectsWeakPassword() {
        assertThatThrownBy(() -> passwordPolicy.validate("weak"))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.USER_004);
    }
}
