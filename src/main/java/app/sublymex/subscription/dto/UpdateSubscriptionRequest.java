package app.sublymex.subscription.dto;

import app.sublymex.subscription.model.BillingCycle;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UpdateSubscriptionRequest(
        @NotBlank @Size(max = 120) String name,
        @NotNull @Positive @Digits(integer = 8, fraction = 2) BigDecimal price,
        @Pattern(regexp = "[A-Za-z]{3}") String currency,
        @NotNull BillingCycle billingCycle,
        @NotNull LocalDate nextRenewal,
        @Size(max = 60) String category,
        @FutureOrPresent LocalDate trialEndsOn,
        @Size(max = 1000) String notes
) {}