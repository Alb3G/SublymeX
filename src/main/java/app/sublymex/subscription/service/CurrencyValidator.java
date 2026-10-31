package app.sublymex.subscription.service;

import app.sublymex.subscription.exception.InvalidCurrencyException;

import java.util.Currency;
import java.util.Locale;

public final class CurrencyValidator {

    private CurrencyValidator() {}

    public static String validate(String currency)
    {
        if (currency == null || currency.isBlank()) {
            return "EUR";
        }

        try {
            return Currency.getInstance(currency.toUpperCase(Locale.ROOT)).getCurrencyCode();
        } catch (IllegalArgumentException e) {
            throw new InvalidCurrencyException("Unknown currency code: '" + currency + "'", e);
        }
    }
}

/*
    For testing.
    assertThatThrownBy(() -> CurrencyValidator.validate("XYZ"))
        .isInstanceOf(InvalidCurrencyException.class);
* */