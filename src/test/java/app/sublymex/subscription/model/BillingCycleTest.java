package app.sublymex.subscription.model;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;

class BillingCycleTest
{
    @ParameterizedTest
    @CsvSource({
            "WEEKLY, WEEKS",
            "MONTHLY, MONTHS",
            "YEARLY, YEARS"
    })
    void mapsEachCycleToItsChronoUnit(BillingCycle cycle, ChronoUnit expected)
    {
        assertThat(cycle.unit()).isEqualTo(expected);
    }
}