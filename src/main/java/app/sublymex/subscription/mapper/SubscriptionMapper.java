package app.sublymex.subscription.mapper;


import app.sublymex.subscription.dto.CreateSubscriptionRequest;
import app.sublymex.subscription.dto.SubscriptionResponse;
import app.sublymex.subscription.model.Subscription;

import java.util.List;
import java.util.UUID;

public final class SubscriptionMapper
{

    private SubscriptionMapper() {}

    /**
     * Mapper uses all fields from CreateSubscriptionRequest to populate the entity, that will be saved into the DB
     * besides currency, status and nextRenewal. Those fields are key to the behavior of Subscription feature so the
     * SubscriptionService will be in charge of doing the propper calculations to populate them later.
     * */
    public static Subscription toSubscription(CreateSubscriptionRequest subscriptionRequest, UUID userId)
    {
        Subscription subscription = new Subscription();
        subscription.setUserId(userId);
        subscription.setName(subscriptionRequest.name());
        subscription.setPrice(subscriptionRequest.price());
        subscription.setBillingCycle(subscriptionRequest.billingCycle());
        subscription.setCategory(subscriptionRequest.category());
        subscription.setTrialEndsOn(subscriptionRequest.trialEndsOn());
        subscription.setNotes(subscriptionRequest.notes());

        return subscription;
    }

    public static SubscriptionResponse toSubscriptionResponse(Subscription subscription, List<String> warnings)
    {
        return new SubscriptionResponse(
                subscription.getId(),
                subscription.getName(),
                subscription.getPrice(),
                subscription.getCurrency(),
                subscription.getBillingCycle(),
                subscription.getNextRenewal(),
                subscription.getCategory(),
                subscription.getStatus(),
                subscription.getTrialEndsOn(),
                subscription.getNotes(),
                subscription.getCreatedAt(),
                warnings
        );
    }

    public static SubscriptionResponse toSubscriptionResponse(Subscription subscription)
    {
        return toSubscriptionResponse(subscription, List.of());
    }
}
