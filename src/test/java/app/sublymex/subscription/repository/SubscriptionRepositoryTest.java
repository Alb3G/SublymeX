package app.sublymex.subscription.repository;

import app.sublymex.subscription.model.BillingCycle;
import app.sublymex.subscription.model.Subscription;
import app.sublymex.subscription.model.SubscriptionStatus;
import jakarta.persistence.EntityManager;
import org.assertj.core.util.BigDecimalComparator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import app.sublymex.TestcontainersConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@Import(TestcontainersConfiguration.class)
public class SubscriptionRepositoryTest
{

    private final SubscriptionRepository subscriptionRepository;
    private final JdbcTemplate jdbcTemplate;
    private final EntityManager entityManager;
    private UUID userId1;
    private UUID userId2;
    private Subscription subscription1;
    private Subscription subscription2;
    private Subscription subscription3;

    @Autowired
    public SubscriptionRepositoryTest(
            SubscriptionRepository subscriptionRepository,
            JdbcTemplate jdbcTemplate,
            EntityManager entityManager
    )
    {
        this.subscriptionRepository = subscriptionRepository;
        this.jdbcTemplate = jdbcTemplate;
        this.entityManager = entityManager;
    }

    @BeforeEach
    void setUpMockData()
    {
        this.userId1 = setUpUserReturningId("testuser1@gmail.com", "hashedpass1", "calendar-token-1");
        this.userId2 = setUpUserReturningId("testuser2@gmail.com", "hashedpass2", "calendar-token-2");

        this.subscription1 = createSubscription(userId1, "Netflix",
                new BigDecimal("12.99"),BillingCycle.MONTHLY,
                LocalDate.of(2026, 11, 1)
        );
        this.subscription2 = createSubscription(userId1, "Spotify",
                new BigDecimal("10.99"), BillingCycle.MONTHLY,
                LocalDate.of(2026, 11, 5)
        );
        this.subscription3 = createSubscription(userId2, "Amazon Prime",
                new BigDecimal("49.90"), BillingCycle.YEARLY,
                LocalDate.of(2027, 3, 15)
        );

        // Flush send to the database the cached data it currently has
        entityManager.flush();

        // Clear forces the next statement to read from the DB
        entityManager.clear();
    }

    /**
     * Test case passed with currency and status having default values have it in mind if someone changes those values
     * */
    @Test
    void findByUserIdReturnsOnlyThatUsersSubscriptions()
    {
        List<Subscription> user1Subscriptions = subscriptionRepository.findByUserId(userId1);
        List<Subscription> user2Subscriptions = subscriptionRepository.findByUserId(userId2);

        assertThat(user1Subscriptions).hasSize(2);
        assertThat(user1Subscriptions)
                .extracting(Subscription::getName).containsExactlyInAnyOrder("Netflix", "Spotify");
        assertThat(user1Subscriptions)
                .extracting(Subscription::getBillingCycle).containsExactlyInAnyOrder(BillingCycle.MONTHLY, BillingCycle.MONTHLY);
        assertThat(user1Subscriptions).extracting(Subscription::getPrice)
                .usingElementComparator(new BigDecimalComparator())
                .contains(new BigDecimal("12.99"), new BigDecimal("10.99"));
        assertThat(user1Subscriptions).extracting(Subscription::getCurrency).containsExactlyInAnyOrder("EUR", "EUR");
        assertThat(user1Subscriptions).extracting(Subscription::getStatus).containsExactlyInAnyOrder(SubscriptionStatus.ACTIVE, SubscriptionStatus.ACTIVE);

        assertThat(user2Subscriptions).hasSize(1);
        assertThat(user2Subscriptions).extracting(Subscription::getName)
                .containsExactlyInAnyOrder("Amazon Prime");
        assertThat(user2Subscriptions)
                .extracting(Subscription::getBillingCycle).containsExactlyInAnyOrder(BillingCycle.YEARLY);
        assertThat(user2Subscriptions).extracting(Subscription::getPrice)
                .usingElementComparator(new BigDecimalComparator())
                .contains(new BigDecimal("49.90"));
        assertThat(user2Subscriptions).extracting(Subscription::getCurrency).containsExactlyInAnyOrder("EUR");
        assertThat(user2Subscriptions).extracting(Subscription::getStatus).containsExactlyInAnyOrder(SubscriptionStatus.ACTIVE);

    }

    @Test
    void returnsSubscriptionWhenUserOwnsIt()
    {
        Optional<Subscription> result = subscriptionRepository.findByIdAndUserId(subscription1.getId(), userId1);
        Optional<Subscription> result2 = subscriptionRepository.findByIdAndUserId(subscription2.getId(), userId1);
        Optional<Subscription> result3 = subscriptionRepository.findByIdAndUserId(subscription3.getId(), userId2);

        assertThat(result).isPresent();
        assertThat(result).hasValueSatisfying(s -> {
            assertThat(s).extracting(Subscription::getName).isEqualTo("Netflix");
            assertThat(s).extracting(Subscription::getUserId).isEqualTo(userId1);
        });
        assertThat(result2).isPresent();
        assertThat(result2).hasValueSatisfying(s -> {
            assertThat(s).extracting(Subscription::getName).isEqualTo("Spotify");
            assertThat(s).extracting(Subscription::getUserId).isEqualTo(userId1);
        });
        assertThat(result3).isPresent();
        assertThat(result3).hasValueSatisfying(s -> {
            assertThat(s).extracting(Subscription::getName).isEqualTo("Amazon Prime");
            assertThat(s).extracting(Subscription::getUserId).isEqualTo(userId2);
        });
    }

    @Test
    void returnsEmptyWhenSubscriptionBelongsToAnotherUser()
    {
        Optional<Subscription> result1 = subscriptionRepository.findByIdAndUserId(subscription3.getId(), userId1);
        Optional<Subscription> result2 = subscriptionRepository.findByIdAndUserId(subscription1.getId(), userId2);
        Optional<Subscription> result3 = subscriptionRepository.findByIdAndUserId(subscription2.getId(), userId2);

        assertThat(result1).isEmpty();
        assertThat(result2).isEmpty();
        assertThat(result3).isEmpty();
    }

    private UUID setUpUserReturningId(String email, String passwordHash, String calendarToken)
    {
        String insertStatement = "INSERT INTO users (email, password_hash, calendar_token) values (?, ?, ?) RETURNING id;";
        return jdbcTemplate.queryForObject(insertStatement, UUID.class,email, passwordHash, calendarToken);
    }

    /**
     * Created Subscription only with not null and unique values
     * */
    private Subscription createSubscription(
            UUID userId,
            String name,
            BigDecimal price,
            BillingCycle billingCycle,
            LocalDate nextRenewal
    )
    {
        Subscription s = new Subscription();

        s.setUserId(userId);
        s.setName(name);
        s.setPrice(price);
        s.setBillingCycle(billingCycle);
        s.setNextRenewal(nextRenewal);

        subscriptionRepository.save(s);

        return s;
    }


}
