package app.sublymex.subscription.service;

import app.sublymex.subscription.model.SubscriptionStatus;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class StatusValidatorTest {

    @ParameterizedTest
    @CsvSource(value = {
            "null,       2026-10-25, ACTIVE",   // sin prueba
            "2026-10-20, 2026-10-25, ACTIVE",   // prueba ya terminada
            "2026-10-25, 2026-10-25, TRIAL",    // termina hoy: aún cuenta
            "2026-10-30, 2026-10-25, TRIAL"     // quedan días
    }, nullValues = "null")
    void derivesStatusFromTrialEnd(LocalDate trialEndsOn, LocalDate today, SubscriptionStatus expected) {
        assertThat(StatusValidator.statusFor(trialEndsOn, today)).isEqualTo(expected);
    }
}