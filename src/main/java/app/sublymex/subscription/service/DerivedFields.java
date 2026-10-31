package app.sublymex.subscription.service;

import app.sublymex.subscription.model.Subscription;
import app.sublymex.subscription.model.SubscriptionStatus;

import java.time.LocalDate;
import java.util.List;

record DerivedFields(
        String currency,
        LocalDate nextRenewal,
        SubscriptionStatus status,
        List<String> warnings
)
{
    void applyTo(Subscription subscription)
    {
        subscription.setCurrency(currency);
        subscription.setNextRenewal(nextRenewal);
        subscription.setStatus(status);
    }
}
