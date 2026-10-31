package app.sublymex.subscription.service;

import app.sublymex.subscription.model.SubscriptionStatus;

import java.time.LocalDate;

public final class StatusValidator {

    private StatusValidator() {}

    public static SubscriptionStatus statusFor(LocalDate trialEndsOn, LocalDate today) {
        if (trialEndsOn == null || trialEndsOn.isBefore(today)) {
            return SubscriptionStatus.ACTIVE;
        }

        return SubscriptionStatus.TRIAL;
    }

}
