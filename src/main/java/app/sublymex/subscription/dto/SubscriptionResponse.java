package app.sublymex.subscription.dto;

import app.sublymex.subscription.model.BillingCycle;
import app.sublymex.subscription.model.SubscriptionStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record SubscriptionResponse(
        Long id,
        String name,
        BigDecimal price,
        String currency,
        BillingCycle billingCycle,
        LocalDate nextRenewal,
        String category,
        SubscriptionStatus status,
        LocalDate trialEndsOn,
        String notes,
        Instant createdAt,
        List<String> warnings
)
{
    public SubscriptionResponse
    {
        warnings = (warnings == null) ? List.of() : List.copyOf(warnings);
    }
}
