package app.sublymex.subscription.model;

import java.time.temporal.ChronoUnit;

public enum BillingCycle {
    WEEKLY(ChronoUnit.WEEKS),
    MONTHLY(ChronoUnit.MONTHS),
    YEARLY(ChronoUnit.YEARS);

    private final ChronoUnit chronoUnit;

    BillingCycle(ChronoUnit chronoUnit)
    {
        this.chronoUnit = chronoUnit;
    }

    public ChronoUnit unit()
    {
        return chronoUnit;
    }
}
