package app.sublymex.subscription.dto;

import app.sublymex.subscription.model.BillingCycle;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateSubscriptionRequest(
        @NotBlank @Size(max = 255) String name,
        @NotNull @Positive @Digits(integer = 8, fraction = 2) BigDecimal price,
        @Pattern(regexp = "[A-Za-z]{3}") String currency,
        @NotNull BillingCycle billingCycle,
        @NotNull LocalDate nextRenewal,
        @Size(max = 60) String category,
        @FutureOrPresent LocalDate trialEndsOn,
        @Size(max = 1000) String notes
)
{

}
