package app.sublymex.subscription.service;


import app.sublymex.subscription.dto.CreateSubscriptionRequest;
import app.sublymex.subscription.dto.SubscriptionResponse;
import app.sublymex.subscription.mapper.SubscriptionMapper;
import app.sublymex.subscription.model.Subscription;
import app.sublymex.subscription.model.SubscriptionStatus;
import app.sublymex.subscription.model.WarningCode;
import app.sublymex.subscription.repository.SubscriptionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;


@Service
public class SubscriptionService
{
    private final SubscriptionRepository subscriptionRepository;
    private final Clock clock;

    public SubscriptionService(SubscriptionRepository subscriptionRepository,
                               Clock clock) {
        this.subscriptionRepository = subscriptionRepository;
        this.clock = clock;
    }

    // Create subscription
    @Transactional
    public SubscriptionResponse create(CreateSubscriptionRequest request, UUID userId)
    {
        String currency = CurrencyValidator.validate(request.currency());

        Subscription subscription = SubscriptionMapper.toSubscription(request, userId);

        LocalDate today = LocalDate.ofInstant(clock.instant(), clock.getZone());
        LocalDate nextRenewal = RenewalCalculator.nextRenewal(request.nextRenewal(), request.billingCycle(), today);
        SubscriptionStatus status = StatusValidator.statusFor(request.trialEndsOn(), today);
        List<String> warnings = new ArrayList<>();

        // Generation of warning's list
        if (nextRenewal.isAfter(request.nextRenewal())) {
            warnings.add(WarningCode.RENEWAL_ADJUSTED.name());
        }

        subscription.setCurrency(currency);
        subscription.setNextRenewal(nextRenewal);
        subscription.setStatus(status);

        Subscription saved = subscriptionRepository.save(subscription);

        return SubscriptionMapper.toSubscriptionResponse(saved, warnings);
    }

    // List user's subscriptions
//    public List<SubscriptionResponse> list(Long userId) {}
    // Get subscription by ID
//    public Optional<SubscriptionResponse> getById(Long id, Long userId) {}
}
