package com.yabo.addressbook.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LoginAttemptServiceTest {

    private LoginAttemptService service;

    @BeforeEach
    void setUp() {
        service = new LoginAttemptService();
    }

    @Test
    void loginFailed_shouldIncrementAttempts() {
        service.loginFailed("1.2.3.4");
        assertThat(service.getRemainingAttempts("1.2.3.4")).isEqualTo(2);
        service.loginFailed("1.2.3.4");
        assertThat(service.getRemainingAttempts("1.2.3.4")).isEqualTo(1);
    }

    @Test
    void loginFailed_shouldLockAfter3Attempts() {
        service.loginFailed("1.2.3.4");
        service.loginFailed("1.2.3.4");
        service.loginFailed("1.2.3.4");
        assertThat(service.isLocked("1.2.3.4")).isTrue();
        assertThat(service.getRemainingAttempts("1.2.3.4")).isEqualTo(0);
    }

    @Test
    void getRemainingAttempts_shouldReturnMaxForUnknownIp() {
        assertThat(service.getRemainingAttempts("unknown")).isEqualTo(3);
    }

    @Test
    void getLockTimeRemainingSeconds_shouldReturnPositiveForLockedIp() {
        service.loginFailed("1.2.3.4");
        service.loginFailed("1.2.3.4");
        service.loginFailed("1.2.3.4");
        assertThat(service.getLockTimeRemainingSeconds("1.2.3.4")).isGreaterThan(0L);
        assertThat(service.getLockTimeRemainingSeconds("1.2.3.4")).isLessThanOrEqualTo(300L);
    }

    @Test
    void getLockTimeRemainingSeconds_shouldReturnZeroForNonLockedIp() {
        assertThat(service.getLockTimeRemainingSeconds("1.2.3.4")).isEqualTo(0);
    }

    @Test
    void loginSucceeded_shouldClearAttempts() {
        service.loginFailed("1.2.3.4");
        service.loginFailed("1.2.3.4");
        service.loginSucceeded("1.2.3.4");
        assertThat(service.isLocked("1.2.3.4")).isFalse();
        assertThat(service.getRemainingAttempts("1.2.3.4")).isEqualTo(3);
    }

    @Test
    void isLocked_shouldReturnFalseForUnknownIp() {
        assertThat(service.isLocked("unknown")).isFalse();
    }

    @Test
    void isLocked_shouldReturnFalse_whenLessThan3Attempts() {
        service.loginFailed("1.2.3.4");
        service.loginFailed("1.2.3.4");
        assertThat(service.isLocked("1.2.3.4")).isFalse();
    }

    @Test
    void loginFailed_shouldHandleMultipleIPs() {
        service.loginFailed("1.1.1.1");
        service.loginFailed("2.2.2.2");
        assertThat(service.getRemainingAttempts("1.1.1.1")).isEqualTo(2);
        assertThat(service.getRemainingAttempts("2.2.2.2")).isEqualTo(2);
    }
}
