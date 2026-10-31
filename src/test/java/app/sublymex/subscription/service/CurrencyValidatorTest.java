package app.sublymex.subscription.service;

import app.sublymex.subscription.exception.InvalidCurrencyException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CurrencyValidatorTest {

    @ParameterizedTest
    @CsvSource({
            "USD, USD",
            "usd, USD",
            "eUr, EUR",
            "gbp, GBP"
    })
    void normalizesToUpperCase(String input, String expected) {
        assertThat(CurrencyValidator.validate(input)).isEqualTo(expected);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    void defaultsToEurWhenMissing(String input) {
        assertThat(CurrencyValidator.validate(input)).isEqualTo("EUR");
    }

    @ParameterizedTest
    @ValueSource(strings = {"XYZ", "xyz", "ZZZ"})
    void rejectsUnknownCodes(String input) {
        assertThatThrownBy(() -> CurrencyValidator.validate(input))
                .isInstanceOf(InvalidCurrencyException.class)
                .hasMessageContaining(input)
                .hasCauseInstanceOf(IllegalArgumentException.class);
    }
}