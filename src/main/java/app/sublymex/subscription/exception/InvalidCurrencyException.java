package app.sublymex.subscription.exception;

public class InvalidCurrencyException extends RuntimeException {
    public InvalidCurrencyException(String message, Throwable cause) {
        super(message, cause);
    }
}
