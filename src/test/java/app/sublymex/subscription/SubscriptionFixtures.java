package app.sublymex.subscription;

import app.sublymex.subscription.dto.CreateSubscriptionRequest;
import app.sublymex.subscription.model.BillingCycle;
import app.sublymex.subscription.model.Subscription;
import app.sublymex.subscription.model.SubscriptionStatus;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public final class SubscriptionFixtures
{
    private SubscriptionFixtures() {}

    public static CreateSubscriptionRequest aRequest()
    {
        return new CreateSubscriptionRequest(
                "Netflix",
                new BigDecimal("12.99"),
                "usd",
                BillingCycle.MONTHLY,
                LocalDate.of(2026, 11, 1),
                "Entertainment",
                LocalDate.of(2026, 12, 15),
                "Family plan"
        );
    }

    public static Subscription aSavedSubscription()
    {
        Subscription s = new Subscription();
        s.setUserId(UUID.randomUUID());
        s.setName("Spotify");
        s.setPrice(new BigDecimal("9.99"));
        s.setCurrency("USD");
        s.setBillingCycle(BillingCycle.MONTHLY);
        s.setNextRenewal(LocalDate.of(2026, 11, 5));
        s.setCategory("Music");
        s.setStatus(SubscriptionStatus.ACTIVE);
        s.setTrialEndsOn(LocalDate.of(2026, 12, 1));
        s.setNotes("Duo plan");

        ReflectionTestUtils.setField(s, "id", UUID.fromString("b8dbc060-b50b-4570-85b3-74cce65044d6"));
        ReflectionTestUtils.setField(s, "createdAt", Instant.parse("2026-10-01T10:00:00Z"));
        return s;
    }
}