package org.aether.usage.domain;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "usage_records", indexes = {
        @Index(name = "idx_usage_records_correlation_id", columnList = "correlation_id"),
        @Index(name = "idx_usage_records_created_at", columnList = "created_at"),
        @Index(name = "idx_usage_records_agent_id", columnList = "agent_id")
})
public class UsageRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "trace_id", length = 128)
    private String traceId;

    @Column(name = "correlation_id", nullable = false, length = 128)
    private String correlationId;

    @Column(name = "execution_id", length = 128)
    private String executionId;

    @Column(name = "agent_id")
    private UUID agentId;

    @Column(name = "provider_id")
    private UUID providerId;

    @Column(name = "model_id")
    private UUID modelId;

    @Column(length = 100)
    private String team;

    @Column(length = 50)
    private String environment;

    @Column(name = "input_tokens")
    private Long inputTokens;

    @Column(name = "output_tokens")
    private Long outputTokens;

    @Column(name = "cached_tokens")
    private Long cachedTokens;

    @Column(name = "latency_ms")
    private Long latencyMs;

    @Column(name = "time_to_first_token_ms")
    private Long timeToFirstTokenMs;

    @Column(nullable = false, length = 30)
    private String status;

    @Column(name = "estimated_cost", precision = 19, scale = 8)
    private BigDecimal estimatedCost;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected UsageRecord() {
    }

    public UsageRecord(String traceId, String correlationId, String executionId,
                       UUID agentId, UUID providerId, UUID modelId, String team,
                       String environment, String status, Instant createdAt) {
        if (correlationId == null || correlationId.isBlank()) {
            throw new IllegalArgumentException("Correlation ID must not be blank");
        }
        this.traceId = traceId;
        this.correlationId = correlationId;
        this.executionId = executionId;
        this.agentId = agentId;
        this.providerId = providerId;
        this.modelId = modelId;
        this.team = team;
        this.environment = environment;
        this.status = status;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public String getTraceId() { return traceId; }
    public String getCorrelationId() { return correlationId; }
    public String getExecutionId() { return executionId; }
    public String getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
}
