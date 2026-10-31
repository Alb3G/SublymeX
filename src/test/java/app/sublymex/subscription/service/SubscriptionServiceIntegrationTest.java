package app.sublymex.subscription.service;

import app.sublymex.subscription.dto.UpdateSubscriptionRequest;
import app.sublymex.subscription.model.BillingCycle;
import app.sublymex.subscription.model.Subscription;
import app.sublymex.subscription.repository.SubscriptionRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import app.sublymex.TestcontainersConfiguration;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
public class SubscriptionServiceIntegrationTest
{
    private final SubscriptionService service;
    private final SubscriptionRepository repository;
    private final JdbcTemplate jdbcTemplate;
    private UUID userId;

    @Autowired
    public SubscriptionServiceIntegrationTest(
            SubscriptionService service,
            SubscriptionRepository repository,
            JdbcTemplate jdbcTemplate
    )
    {
        this.service = service;
        this.repository = repository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @BeforeEach
    void setUp()
    {
        userId = jdbcTemplate.queryForObject(
                "INSERT INTO users (email, password_hash, calendar_token) VALUES (?, ?, ?) RETURNING id",
                UUID.class, "it-update@example.com", "hash", "it-update-token");
    }

    @AfterEach
    void tearDown()
    {
        // ON DELETE CASCADE borra también sus suscripciones
        jdbcTemplate.update("DELETE FROM users WHERE id = ?", userId);
    }

    @Test
    void updatePersistsChangesToTheDatabase()
    {
        Subscription s = new Subscription();
        s.setUserId(userId);
        s.setName("Old name");
        s.setPrice(new BigDecimal("1.00"));
        s.setBillingCycle(BillingCycle.YEARLY);
        s.setNextRenewal(LocalDate.now().plusDays(60));
        UUID id = repository.save(s).getId();

        LocalDate newRenewal = LocalDate.now().plusDays(30);
        service.update(
                new UpdateSubscriptionRequest("New name", new BigDecimal("5.50"), "usd",
                        BillingCycle.MONTHLY, newRenewal, "Cat", null, "Some notes"),
                id, userId);

        // Sin transacción exterior: esta lectura va realmente a la base de datos
        Subscription reloaded = repository.findById(id).orElseThrow();
        assertThat(reloaded.getName()).isEqualTo("New name");
        assertThat(reloaded.getPrice()).isEqualByComparingTo("5.50");
        assertThat(reloaded.getCurrency()).isEqualTo("USD");
        assertThat(reloaded.getBillingCycle()).isEqualTo(BillingCycle.MONTHLY);
        assertThat(reloaded.getNextRenewal()).isEqualTo(newRenewal);
        assertThat(reloaded.getCategory()).isEqualTo("Cat");
        assertThat(reloaded.getNotes()).isEqualTo("Some notes");
    }
}