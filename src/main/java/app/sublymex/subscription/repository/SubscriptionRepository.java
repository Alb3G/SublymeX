package app.sublymex.subscription.repository;

import app.sublymex.subscription.model.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SubscriptionRepository extends JpaRepository<Subscription, UUID>
{

    Optional<Subscription> findByIdAndUserId(UUID id, UUID userId);

    List<Subscription> findByUserId(UUID userId);

    List<Subscription> findByUserIdOrderByNextRenewalAsc(UUID userId);
}
