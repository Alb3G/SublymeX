package app.sublymex.common.web;

import app.sublymex.subscription.exception.InvalidCurrencyException;
import app.sublymex.subscription.exception.SubscriptionNotFoundException;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import org.slf4j.Logger;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(SubscriptionNotFoundException.class)
    public ProblemDetail handleSubscriptionNotFound(SubscriptionNotFoundException e) {
        ProblemDetail issue = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        issue.setTitle("Subscription not found");
        issue.setDetail("The requested subscription does not exist");

        log.info(e.getMessage());

        return issue;
    }

    @ExceptionHandler(InvalidCurrencyException.class)
    public ProblemDetail handleInvalidCurrency(InvalidCurrencyException e) {
        ProblemDetail issue = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        issue.setTitle("Invalid currency");
        issue.setDetail("The currency code is not a valid ISO 4217 code");

        log.info(e.getMessage());

        return issue;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationErrors(MethodArgumentNotValidException e) {
        ProblemDetail issue = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        Map<String, String> errors = new LinkedHashMap<>();

        for (FieldError fieldError : e.getBindingResult().getFieldErrors()) {
            errors.putIfAbsent(fieldError.getField(), fieldError.getDefaultMessage());
        }

        issue.setProperty("errors", errors);
        issue.setTitle("Validation failed");
        issue.setDetail("Request validation failed");

        log.info("Validation failed for {} field(s)", errors.size());

        return issue;
    }
}
