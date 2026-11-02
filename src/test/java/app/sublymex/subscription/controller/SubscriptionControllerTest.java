package app.sublymex.subscription.controller;

import app.sublymex.config.SecurityConfig;
import app.sublymex.subscription.dto.CreateSubscriptionRequest;
import app.sublymex.subscription.dto.SubscriptionResponse;
import app.sublymex.subscription.dto.UpdateSubscriptionRequest;
import app.sublymex.subscription.exception.InvalidCurrencyException;
import app.sublymex.subscription.exception.SubscriptionNotFoundException;
import app.sublymex.subscription.model.BillingCycle;
import app.sublymex.subscription.model.SubscriptionStatus;
import app.sublymex.subscription.service.SubscriptionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SubscriptionController.class)
@Import(SecurityConfig.class) // sin esto Spring Security responde 401 a todo
class SubscriptionControllerTest
{
    private static final String USER_HEADER = "X-User-Id";
    private static final UUID USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID SUBSCRIPTION_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");

    private static final String VALID_BODY = """
            {
              "name": "Netflix",
              "price": 12.99,
              "currency": "EUR",
              "billingCycle": "MONTHLY",
              "nextRenewal": "2026-12-01",
              "category": "Entertainment",
              "trialEndsOn": null,
              "notes": "Family plan"
            }
            """;

    private static final String INVALID_BODY = """
            {
              "name": "   ",
              "price": -5,
              "billingCycle": "MONTHLY",
              "nextRenewal": "2026-12-01"
            }
            """;

    private static final String BODY_WITH_PAST_TRIAL = """
        {
          "name": "Netflix",
          "price": 12.99,
          "currency": "EUR",
          "billingCycle": "MONTHLY",
          "nextRenewal": "2026-12-01",
          "category": "Entertainment",
          "trialEndsOn": "2026-10-01",
          "notes": "Family plan"
        }
        """;

    private final MockMvc mockMvc;

    @MockitoBean
    private SubscriptionService subscriptionService;

    @Autowired
    SubscriptionControllerTest(MockMvc mockMvc)
    {
        this.mockMvc = mockMvc;
    }

    // --- GET /subscriptions ---

    @Test
    void listReturnsSubscriptionsOfTheUserInTheHeader() throws Exception
    {
        when(subscriptionService.list(USER_ID)).thenReturn(List.of(aResponse()));

        mockMvc.perform(get("/subscriptions").header(USER_HEADER, USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Netflix"));
    }

    @Test
    void listWithoutUserHeaderIsRejected() throws Exception
    {
        mockMvc.perform(get("/subscriptions"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listWithMalformedUserHeaderIsRejected() throws Exception
    {
        mockMvc.perform(get("/subscriptions").header(USER_HEADER, "not-a-uuid"))
                .andExpect(status().isBadRequest());
    }

    // --- GET /subscriptions/{id} ---

    @Test
    void getByIdReturnsTheSubscription() throws Exception
    {
        when(subscriptionService.getById(SUBSCRIPTION_ID, USER_ID)).thenReturn(aResponse());

        mockMvc.perform(get("/subscriptions/{id}", SUBSCRIPTION_ID).header(USER_HEADER, USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(SUBSCRIPTION_ID.toString()))
                .andExpect(jsonPath("$.currency").value("EUR"));
    }

    @Test
    void getByIdReturns404WithGenericProblemWhenNotFound() throws Exception
    {
        when(subscriptionService.getById(SUBSCRIPTION_ID, USER_ID))
                .thenThrow(new SubscriptionNotFoundException("Subscription not found for id: " + SUBSCRIPTION_ID));

        mockMvc.perform(get("/subscriptions/{id}", SUBSCRIPTION_ID).header(USER_HEADER, USER_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Subscription not found"))
                .andExpect(jsonPath("$.detail").value("The requested subscription does not exist"))
                .andExpect(content().string(not(containsString("Subscription not found for id"))));
    }

    // --- POST /subscriptions ---

    @Test
    void createReturns201WithLocationAndBody() throws Exception
    {
        when(subscriptionService.create(any(CreateSubscriptionRequest.class), eq(USER_ID)))
                .thenReturn(aResponse());

        mockMvc.perform(post("/subscriptions")
                        .header(USER_HEADER, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/subscriptions/" + SUBSCRIPTION_ID)))
                .andExpect(jsonPath("$.id").value(SUBSCRIPTION_ID.toString()));
    }

    @Test
    void createWithInvalidBodyReturns400WithFieldErrors() throws Exception
    {
        mockMvc.perform(post("/subscriptions")
                        .header(USER_HEADER, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(INVALID_BODY))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.errors.name").exists())
                .andExpect(jsonPath("$.errors.price").exists());
    }

    @Test
    void createWithUnknownCurrencyReturns400() throws Exception
    {
        when(subscriptionService.create(any(CreateSubscriptionRequest.class), eq(USER_ID)))
                .thenThrow(new InvalidCurrencyException(
                        "Unknown currency code: 'XYZ'", new IllegalArgumentException("XYZ")));

        mockMvc.perform(post("/subscriptions")
                        .header(USER_HEADER, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid currency"))
                .andExpect(jsonPath("$.detail").value("The currency code is not a valid ISO 4217 code"));
    }

    // --- PUT /subscriptions/{id} ---

    @Test
    void updateReturns200WithTheUpdatedSubscription() throws Exception
    {
        when(subscriptionService.update(any(UpdateSubscriptionRequest.class), eq(SUBSCRIPTION_ID), eq(USER_ID)))
                .thenReturn(aResponse());

        mockMvc.perform(put("/subscriptions/{id}", SUBSCRIPTION_ID)
                        .header(USER_HEADER, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Netflix"));
    }

    @Test
    void updateReturns404WhenNotFound() throws Exception
    {
        when(subscriptionService.update(any(UpdateSubscriptionRequest.class), eq(SUBSCRIPTION_ID), eq(USER_ID)))
                .thenThrow(new SubscriptionNotFoundException("Subscription not found for id: " + SUBSCRIPTION_ID));

        mockMvc.perform(put("/subscriptions/{id}", SUBSCRIPTION_ID)
                        .header(USER_HEADER, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateWithInvalidBodyReturns400() throws Exception
    {
        mockMvc.perform(put("/subscriptions/{id}", SUBSCRIPTION_ID)
                        .header(USER_HEADER, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(INVALID_BODY))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").exists());
    }

    @Test
    void updateAcceptsTrialEndDateInThePast() throws Exception
    {
        when(subscriptionService.update(any(UpdateSubscriptionRequest.class), eq(SUBSCRIPTION_ID), eq(USER_ID)))
                .thenReturn(aResponse());

        mockMvc.perform(put("/subscriptions/{id}", SUBSCRIPTION_ID)
                        .header(USER_HEADER, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY_WITH_PAST_TRIAL))
                .andExpect(status().isOk());

        verify(subscriptionService).update(any(UpdateSubscriptionRequest.class), eq(SUBSCRIPTION_ID), eq(USER_ID));
    }

    @Test
    void createRejectsTrialEndDateInThePast() throws Exception
    {
        mockMvc.perform(post("/subscriptions")
                        .header(USER_HEADER, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY_WITH_PAST_TRIAL))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.trialEndsOn").exists());
    }

    // --- DELETE /subscriptions/{id} ---

    @Test
    void deleteReturns204WithoutBody() throws Exception
    {
        mockMvc.perform(delete("/subscriptions/{id}", SUBSCRIPTION_ID).header(USER_HEADER, USER_ID))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(subscriptionService).delete(SUBSCRIPTION_ID, USER_ID);
    }

    @Test
    void deleteReturns404WhenNotFound() throws Exception
    {
        doThrow(new SubscriptionNotFoundException("Subscription not found for id: " + SUBSCRIPTION_ID))
                .when(subscriptionService).delete(SUBSCRIPTION_ID, USER_ID);

        mockMvc.perform(delete("/subscriptions/{id}", SUBSCRIPTION_ID).header(USER_HEADER, USER_ID))
                .andExpect(status().isNotFound());
    }

    // --- Helper ---

    private static SubscriptionResponse aResponse()
    {
        return new SubscriptionResponse(
                SUBSCRIPTION_ID, "Netflix", new BigDecimal("12.99"), "EUR", BillingCycle.MONTHLY,
                LocalDate.of(2026, 12, 1), "Entertainment", SubscriptionStatus.ACTIVE, null,
                "Family plan", Instant.parse("2026-11-02T12:00:00Z"), List.of());
    }
}