package org.aether.budget.domain;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "budgets", uniqueConstraints = {
        @UniqueConstraint(name = "uk_budget_scope_period", columnNames = {"scope", "scope_key", "period_start", "period_end"})
})
public class Budget {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private BudgetScope scope;

    @Column(name = "scope_key", nullable = false, length = 255)
    private String scopeKey;

    @Column(name = "period_start", nullable = false)
    private LocalDate periodStart;

    @Column(name = "period_end", nullable = false)
    private LocalDate periodEnd;

    @Column(name = "limit_amount", nullable = false, precision = 19, scale = 8)
    private BigDecimal limitAmount;

    @Column(name = "consumed_amount", nullable = false, precision = 19, scale = 8)
    private BigDecimal consumedAmount;

    @Column(nullable = false)
    private boolean enabled;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Budget() {
    }

    public Budget(BudgetScope scope, String scopeKey, LocalDate periodStart,
                  LocalDate periodEnd, BigDecimal limitAmount) {
        if (scope == null || scopeKey == null || scopeKey.isBlank()) throw new IllegalArgumentException("Budget scope is required");
        if (periodEnd.isBefore(periodStart)) throw new IllegalArgumentException("Budget period is invalid");
        if (limitAmount == null || limitAmount.signum() < 0) throw new IllegalArgumentException("Budget limit must not be negative");
        this.scope = scope;
        this.scopeKey = scopeKey.trim();
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
        this.limitAmount = limitAmount;
        this.consumedAmount = BigDecimal.ZERO;
        this.enabled = true;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    public UUID getId() { return id; }
    public BudgetScope getScope() { return scope; }
    public String getScopeKey() { return scopeKey; }
    public LocalDate getPeriodStart() { return periodStart; }
    public LocalDate getPeriodEnd() { return periodEnd; }
    public BigDecimal getLimitAmount() { return limitAmount; }
    public BigDecimal getConsumedAmount() { return consumedAmount; }
    public boolean isEnabled() { return enabled; }

    public void consume(BigDecimal amount) {
        if (amount == null || amount.signum() < 0) throw new IllegalArgumentException("Consumed amount must not be negative");
        this.consumedAmount = this.consumedAmount.add(amount);
        this.updatedAt = Instant.now();
    }

    public boolean isExceeded() { return consumedAmount.compareTo(limitAmount) >= 0; }
}
