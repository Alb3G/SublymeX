package app.sublymex.subscription.service;

import app.sublymex.subscription.dto.CreateSubscriptionRequest;
import app.sublymex.subscription.dto.SubscriptionResponse;
import app.sublymex.subscription.exception.InvalidCurrencyException;
import app.sublymex.subscription.model.BillingCycle;
import app.sublymex.subscription.model.Subscription;
import app.sublymex.subscription.model.SubscriptionStatus;
import app.sublymex.subscription.model.WarningCode;
import app.sublymex.subscription.repository.SubscriptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {

    private static final UUID USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final LocalDate TODAY = LocalDate.of(2026, 10, 25);

    @Mock
    private SubscriptionRepository repository;

    private SubscriptionService service;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(Instant.parse("2026-10-25T10:00:00Z"), ZoneOffset.UTC);
        service = new SubscriptionService(repository, clock);

        // lenient: el test de moneda inválida no llega a guardar y Mockito lo marcaría como stub sin usar.
        lenient().when(repository.save(any(Subscription.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    // --- Renewal & Warnings ---

    @Test
    void keepsFutureRenewalWithoutWarnings() {
        SubscriptionResponse response = service.create(
                request(LocalDate.of(2026, 11, 1), "USD", null), USER_ID);

        assertThat(response.nextRenewal()).isEqualTo(LocalDate.of(2026, 11, 1));
        assertThat(response.warnings()).isEmpty();
    }

    @Test
    void keepsRenewalDueTodayWithoutWarnings() {
        SubscriptionResponse response = service.create(request(TODAY, "USD", null), USER_ID);

        assertThat(response.nextRenewal()).isEqualTo(TODAY);
        assertThat(response.warnings()).isEmpty();
    }

    @Test
    void rollsPastRenewalForwardAndWarns() {
        SubscriptionResponse response = service.create(
                request(LocalDate.of(2026, 10, 1), "USD", null), USER_ID);

        assertThat(response.nextRenewal()).isEqualTo(LocalDate.of(2026, 11, 1));
        assertThat(response.warnings()).containsExactly(WarningCode.RENEWAL_ADJUSTED.name());
    }

    // --- Status ---

    @Test
    void marksSubscriptionAsTrialWhenTrialIsStillRunning() {
        SubscriptionResponse response = service.create(
                request(LocalDate.of(2026, 11, 1), "USD", LocalDate.of(2026, 11, 10)), USER_ID);

        assertThat(response.status()).isEqualTo(SubscriptionStatus.TRIAL);
    }

    @Test
    void marksSubscriptionAsActiveWithoutTrial() {
        SubscriptionResponse response = service.create(
                request(LocalDate.of(2026, 11, 1), "USD", null), USER_ID);

        assertThat(response.status()).isEqualTo(SubscriptionStatus.ACTIVE);
    }

    // --- Currency ---

    @Test
    void normalizesCurrencyToUpperCase() {
        SubscriptionResponse response = service.create(
                request(LocalDate.of(2026, 11, 1), "usd", null), USER_ID);

        assertThat(response.currency()).isEqualTo("USD");
    }

    @Test
    void defaultsCurrencyToEurWhenMissing() {
        SubscriptionResponse response = service.create(
                request(LocalDate.of(2026, 11, 1), null, null), USER_ID);

        assertThat(response.currency()).isEqualTo("EUR");
    }

    @Test
    void rejectsInvalidCurrencyWithoutSaving() {
        CreateSubscriptionRequest invalid = request(LocalDate.of(2026, 11, 1), "XYZ", null);

        assertThatThrownBy(() -> service.create(invalid, USER_ID))
                .isInstanceOf(InvalidCurrencyException.class);

        verify(repository, never()).save(any());
        verifyNoInteractions(repository);
    }

    // --- Lo que se guarda ---

    @Test
    void savesEntityWithRequestDataAndServiceDecidedFields() {
        service.create(request(LocalDate.of(2026, 10, 1), "usd", LocalDate.of(2026, 11, 10)), USER_ID);

        ArgumentCaptor<Subscription> captor = ArgumentCaptor.forClass(Subscription.class);
        verify(repository).save(captor.capture());
        Subscription saved = captor.getValue();

        assertThat(saved.getUserId()).isEqualTo(USER_ID);
        assertThat(saved.getName()).isEqualTo("Netflix");
        assertThat(saved.getPrice()).isEqualByComparingTo("12.99");
        assertThat(saved.getBillingCycle()).isEqualTo(BillingCycle.MONTHLY);
        assertThat(saved.getCurrency()).isEqualTo("USD");
        assertThat(saved.getNextRenewal()).isEqualTo(LocalDate.of(2026, 11, 1));
        assertThat(saved.getStatus()).isEqualTo(SubscriptionStatus.TRIAL);
    }

    // --- Helper ---

    private static CreateSubscriptionRequest request(LocalDate nextRenewal, String currency, LocalDate trialEndsOn) {
        return new CreateSubscriptionRequest(
                "Netflix",
                new BigDecimal("12.99"),
                currency,
                BillingCycle.MONTHLY,
                nextRenewal,
                "Entertainment",
                trialEndsOn,
                "Family plan"
        );
    }
}