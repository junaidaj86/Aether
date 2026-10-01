package org.aether.routing.domain;

import jakarta.persistence.*;
import org.aether.agent.domain.Agent;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "routing_policies", uniqueConstraints = {
        @UniqueConstraint(name = "uk_routing_policy_agent_name", columnNames = {"agent_id", "name"})
})
public class RoutingPolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "agent_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_routing_policy_agent"))
    private Agent agent;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private RoutingStrategy strategy;

    @Column(name = "max_latency_ms")
    private Integer maxLatencyMs;

    @Column(name = "max_cost_per_request", precision = 19, scale = 8)
    private BigDecimal maxCostPerRequest;

    @Column(nullable = false)
    private boolean enabled;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected RoutingPolicy() {
    }

    public RoutingPolicy(Agent agent, String name, RoutingStrategy strategy,
                         Integer maxLatencyMs, BigDecimal maxCostPerRequest) {
        if (agent == null) throw new IllegalArgumentException("Agent must not be null");
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Policy name must not be blank");
        if (maxLatencyMs != null && maxLatencyMs <= 0) throw new IllegalArgumentException("Max latency must be greater than zero");
        if (maxCostPerRequest != null && maxCostPerRequest.signum() < 0) throw new IllegalArgumentException("Max cost must not be negative");
        this.agent = agent;
        this.name = name.trim();
        this.strategy = strategy;
        this.maxLatencyMs = maxLatencyMs;
        this.maxCostPerRequest = maxCostPerRequest;
        this.enabled = true;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    public UUID getId() { return id; }
    public Agent getAgent() { return agent; }
    public String getName() { return name; }
    public RoutingStrategy getStrategy() { return strategy; }
    public Integer getMaxLatencyMs() { return maxLatencyMs; }
    public BigDecimal getMaxCostPerRequest() { return maxCostPerRequest; }
    public boolean isEnabled() { return enabled; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    public void enable() { this.enabled = true; this.updatedAt = Instant.now(); }
    public void disable() { this.enabled = false; this.updatedAt = Instant.now(); }
}
