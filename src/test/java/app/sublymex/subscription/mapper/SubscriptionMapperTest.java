package app.sublymex.subscription.mapper;

import app.sublymex.subscription.dto.CreateSubscriptionRequest;
import app.sublymex.subscription.dto.SubscriptionResponse;
import app.sublymex.subscription.model.Subscription;
import app.sublymex.subscription.model.SubscriptionStatus;
import org.junit.jupiter.api.Test;

import java.util.List;

import static app.sublymex.subscription.SubscriptionFixtures.aRequest;
import static app.sublymex.subscription.SubscriptionFixtures.aSavedSubscription;
import static app.sublymex.subscription.mapper.SubscriptionMapper.toSubscriptionResponse;
import static org.assertj.core.api.Assertions.assertThat;

public class SubscriptionMapperTest
{

    @Test
    void copiesFieldsFromRequest()
    {
        CreateSubscriptionRequest subscriptionRequest = aRequest();
        Subscription result = SubscriptionMapper.toSubscription(subscriptionRequest,7L);

        assertThat(result.getUserId()).isEqualTo(7L);
        assertThat(result.getName()).isEqualTo(subscriptionRequest.name());
        assertThat(result.getPrice()).isEqualTo(subscriptionRequest.price());
        assertThat(result.getBillingCycle()).isEqualTo(subscriptionRequest.billingCycle());
        assertThat(result.getCategory()).isEqualTo(subscriptionRequest.category());
        assertThat(result.getTrialEndsOn()).isEqualTo(subscriptionRequest.trialEndsOn());
        assertThat(result.getNotes()).isEqualTo(subscriptionRequest.notes());
    }

    @Test
    void leavesServiceDecidedFieldsUntouched()
    {
        CreateSubscriptionRequest subscriptionRequest = aRequest();
        Subscription result = SubscriptionMapper.toSubscription(subscriptionRequest,7L);

        assertThat(result.getId()).isNull();
        assertThat(result.getNextRenewal()).isNull();
        assertThat(result.getCurrency()).isEqualTo("EUR");
        assertThat(result.getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
    }

    @Test
    void copiesAllFieldsAndHasNoWarnings()
    {
        Subscription subscription = aSavedSubscription();
        SubscriptionResponse result = toSubscriptionResponse(subscription);

        assertThat(result.id()).isEqualTo(42L);
        assertThat(result.name()).isEqualTo(subscription.getName());
        assertThat(result.price()).isEqualTo(subscription.getPrice());
        assertThat(result.currency()).isEqualTo(subscription.getCurrency());
        assertThat(result.billingCycle()).isEqualTo(subscription.getBillingCycle());
        assertThat(result.nextRenewal()).isEqualTo(subscription.getNextRenewal());
        assertThat(result.category()).isEqualTo(subscription.getCategory());
        assertThat(result.status()).isEqualTo(subscription.getStatus());
        assertThat(result.trialEndsOn()).isEqualTo(subscription.getTrialEndsOn());
        assertThat(result.notes()).isEqualTo(subscription.getNotes());
        assertThat(result.createdAt()).isEqualTo(subscription.getCreatedAt());
        assertThat(result.warnings()).isEmpty();
    }

    @Test
    void keepsWarningsPassedByCaller()
    {
        Subscription subscription = aSavedSubscription();
        SubscriptionResponse result = toSubscriptionResponse(subscription, List.of("first warning", "second warning"));

        assertThat(result.warnings()).containsExactly("first warning", "second warning");
    }

}
