package app.sublymex.subscription.service;


import app.sublymex.subscription.repository.SubscriptionRepository;
import org.springframework.stereotype.Service;
import java.time.Clock;


@Service
public class SubscriptionService
{
    private final SubscriptionRepository subscriptionRepository;
    private final Clock clock;

    public SubscriptionService(SubscriptionRepository subscriptionRepository, Clock clock) {
        this.subscriptionRepository = subscriptionRepository;
        this.clock = clock;
    }

    // Create subscription
//    @Transactional
//    public SubscriptionResponse create(CreateSubscriptionRequest createSubscriptionRequest, Long userId)
//    {
//        Instant today = clock.instant();
//
//    }
//
//    // List user's subscriptions
//    public List<SubscriptionResponse> list(Long userId) {}
//    // Get subscription by ID
//    public Optional<SubscriptionResponse> getById(Long id, Long userId) {}
}
