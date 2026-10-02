package org.aether.audit.domain;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "audit_events", indexes = {
        @Index(name = "idx_audit_events_correlation_id", columnList = "correlation_id"),
        @Index(name = "idx_audit_events_created_at", columnList = "created_at"),
        @Index(name = "idx_audit_events_resource", columnList = "resource_type, resource_id")
})
public class AuditEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;

    @Column(nullable = false, length = 255)
    private String actor;

    @Column(name = "resource_type", nullable = false, length = 100)
    private String resourceType;

    @Column(name = "resource_id")
    private UUID resourceId;

    @Column(name = "trace_id", length = 128)
    private String traceId;

    @Column(name = "correlation_id", length = 128)
    private String correlationId;

    @Column(name = "details", columnDefinition = "TEXT")
    private String details;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected AuditEvent() {
    }

    public AuditEvent(String eventType, String actor, String resourceType,
                      UUID resourceId, String traceId, String correlationId,
                      String details, Instant createdAt) {
        this.eventType = requireText(eventType, "Audit event type must not be blank");
        this.actor = requireText(actor, "Audit actor must not be blank");
        this.resourceType = requireText(resourceType, "Audit resource type must not be blank");
        this.resourceId = resourceId;
        this.traceId = traceId;
        this.correlationId = correlationId;
        this.details = details;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public String getEventType() { return eventType; }
    public String getActor() { return actor; }
    public String getResourceType() { return resourceType; }
    public UUID getResourceId() { return resourceId; }
    public String getTraceId() { return traceId; }
    public String getCorrelationId() { return correlationId; }
    public String getDetails() { return details; }
    public Instant getCreatedAt() { return createdAt; }

    private static String requireText(String value, String message) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(message);
        return value.trim();
    }
}
