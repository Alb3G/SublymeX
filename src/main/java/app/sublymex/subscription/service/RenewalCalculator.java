package app.sublymex.subscription.service;

import app.sublymex.subscription.model.BillingCycle;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public final class RenewalCalculator {

    public RenewalCalculator() {}

    public static LocalDate nextRenewal(LocalDate dayOfPayment, BillingCycle subscriptionCycle, LocalDate today)
    {
        if (!dayOfPayment.isBefore(today)) {
            return dayOfPayment;
        }

        ChronoUnit unit = subscriptionCycle.unit();
        long numberOfCycles = unit.between(dayOfPayment, today);
        LocalDate lastRenewal = dayOfPayment.plus(numberOfCycles, unit);

        if (lastRenewal.isBefore(today)) {
            return dayOfPayment.plus(numberOfCycles + 1, unit);
        }

        return lastRenewal;
    }
}
