package app.sublymex.subscription.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "subscriptions")
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private UUID id;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "name")
    private String name;

    @Column(name = "price")
    private BigDecimal price;

    @Column(name = "currency")
    private String currency = "EUR";

    @Enumerated(EnumType.STRING)
    @Column(name = "billing_cycle")
    private BillingCycle billingCycle;

    @Column(name = "next_renewal")
    private LocalDate nextRenewal;

    @Column(name = "category")
    private String category;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private SubscriptionStatus status = SubscriptionStatus.ACTIVE;

    @Column(name = "trial_ends_on")
    private LocalDate trialEndsOn;

    @Column(name = "notes")
    private String notes;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public BillingCycle getBillingCycle() {
        return billingCycle;
    }

    public void setBillingCycle(BillingCycle billingCycle) {
        this.billingCycle = billingCycle;
    }

    public LocalDate getNextRenewal() {
        return nextRenewal;
    }

    public void setNextRenewal(LocalDate nextRenewal) {
        this.nextRenewal = nextRenewal;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public SubscriptionStatus getStatus() {
        return status;
    }

    public void setStatus(SubscriptionStatus status) {
        this.status = status;
    }

    public LocalDate getTrialEndsOn() {
        return trialEndsOn;
    }

    public void setTrialEndsOn(LocalDate trialEndsOn) {
        this.trialEndsOn = trialEndsOn;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
