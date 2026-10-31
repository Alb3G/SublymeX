package app.sublymex.subscription.service;


import app.sublymex.subscription.dto.CreateSubscriptionRequest;
import app.sublymex.subscription.dto.SubscriptionResponse;
import app.sublymex.subscription.dto.UpdateSubscriptionRequest;
import app.sublymex.subscription.exception.SubscriptionNotFoundException;
import app.sublymex.subscription.mapper.SubscriptionMapper;
import app.sublymex.subscription.model.BillingCycle;
import app.sublymex.subscription.model.Subscription;
import app.sublymex.subscription.model.SubscriptionStatus;
import app.sublymex.subscription.model.WarningCode;
import app.sublymex.subscription.repository.SubscriptionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
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

    @Transactional
    public SubscriptionResponse create(CreateSubscriptionRequest request, UUID userId)
    {
        DerivedFields derived = deriveFields(
                request.currency(), request.nextRenewal(), request.billingCycle(), request.trialEndsOn());

        Subscription subscription = SubscriptionMapper.toSubscription(request, userId);
        derived.applyTo(subscription);

        Subscription saved = subscriptionRepository.save(subscription);

        return SubscriptionMapper.toSubscriptionResponse(saved, derived.warnings());
    }

    @Transactional(readOnly = true)
    public List<SubscriptionResponse> list(UUID userId)
    {
        return subscriptionRepository.findByUserIdOrderByNextRenewalAsc(userId).stream()
                .map(SubscriptionMapper::toSubscriptionResponse).toList();
    }

    @Transactional(readOnly = true)
    public SubscriptionResponse getById(UUID id, UUID userId) {
        return subscriptionRepository.findByIdAndUserId(id, userId)
                .map(SubscriptionMapper::toSubscriptionResponse)
                .orElseThrow(() -> new SubscriptionNotFoundException("Subscription not found for id: " + id));
    }

    @Transactional
    public SubscriptionResponse update(UpdateSubscriptionRequest request, UUID id, UUID userId)
    {
        Subscription subscription = subscriptionRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new SubscriptionNotFoundException("Subscription not found for id: " + id));

        DerivedFields derived = deriveFields(
                request.currency(), request.nextRenewal(), request.billingCycle(), request.trialEndsOn());

        subscription.setName(request.name());
        subscription.setPrice(request.price());
        subscription.setBillingCycle(request.billingCycle());
        subscription.setCategory(request.category());
        subscription.setTrialEndsOn(request.trialEndsOn());
        subscription.setNotes(request.notes());
        derived.applyTo(subscription);

        return SubscriptionMapper.toSubscriptionResponse(subscription, derived.warnings());
    }

    private DerivedFields deriveFields(
            String currency,
            LocalDate requestedRenewal,
            BillingCycle billingCycle,
            LocalDate trialEndsOn
    )
    {
        String validCurrency = CurrencyValidator.validate(currency);
        LocalDate today = LocalDate.ofInstant(clock.instant(), clock.getZone());
        LocalDate nextRenewal = RenewalCalculator.nextRenewal(requestedRenewal, billingCycle, today);
        SubscriptionStatus status = StatusValidator.statusFor(trialEndsOn, today);
        List<String> warnings = new ArrayList<>();

        if (nextRenewal.isAfter(requestedRenewal)) {
            warnings.add(WarningCode.RENEWAL_ADJUSTED.name());
        }

        return new DerivedFields(validCurrency, nextRenewal, status, warnings);
    }
}
