package app.sublymex.subscription.service;

import app.sublymex.subscription.model.BillingCycle;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class RenewalCalculatorTest
{
    @ParameterizedTest(name = "{0} {1}, today {2} -> {3}")
    @CsvSource({
            // original,   cycle,   today,      expected

            // today or a date in the future is returned as it is
            "2026-10-25, WEEKLY,  2026-10-18, 2026-10-25",
            "2026-10-18, WEEKLY,  2026-10-18, 2026-10-18",
            "2026-12-01, MONTHLY, 2026-10-18, 2026-12-01",
            "2027-03-05, YEARLY,  2026-10-18, 2027-03-05",

            // past date: forward the date to the next cycle
            "2026-10-17, MONTHLY, 2026-10-18, 2026-11-17",
            "2026-10-01, WEEKLY,  2026-10-18, 2026-10-22",
            "2020-01-01, YEARLY,  2026-10-24, 2027-01-01",

            // the spot lands just in today, returned as it is
            "2026-10-11, WEEKLY,  2026-10-18, 2026-10-18",
            "2026-09-18, MONTHLY, 2026-10-18, 2026-10-18",

            // days that doesn't exist in destination month.
            "2026-01-31, MONTHLY, 2026-03-01, 2026-03-31",
            "2026-01-30, MONTHLY, 2026-02-15, 2026-02-28",
            "2024-02-29, YEARLY,  2026-10-18, 2027-02-28"
    })
    void calculatesNextRenewal(LocalDate original, BillingCycle cycle,
                               LocalDate today, LocalDate expected)
    {
        LocalDate result = RenewalCalculator.nextRenewal(original, cycle, today);

        assertThat(result).isEqualTo(expected);
    }
}