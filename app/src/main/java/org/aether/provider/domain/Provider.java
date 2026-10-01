package org.aether.provider.domain;

import jakarta.persistence.*;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.Objects;

@Entity
@Table(
    name = "providers",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_provider_name_environment",
            columnNames = {"name", "environment"}
        )
    }
)
@NoArgsConstructor
public class Provider {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider_type", nullable = false, length = 50)
    private ProviderType type;

    @Column(name = "base_url", nullable = false, length = 500)
    private String baseUrl;

    @Column(nullable = false, length = 50)
    private String environment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ProviderStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;


    public Provider(
            String name,
            ProviderType type,
            String baseUrl,
            String environment) {

        this.id = UUID.randomUUID();
        this.name = name;
        this.type = type;
        this.baseUrl = baseUrl;
        this.environment = environment;
        this.status = ProviderStatus.ACTIVE;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public ProviderType getType() {
        return type;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getEnvironment() {
        return environment;
    }

    public ProviderStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void update(String name, ProviderType type, String baseUrl, String environment) {

        this.name = name;
        this.type = type;
        this.baseUrl = baseUrl;
        this.environment = environment;
        this.updatedAt = Instant.now();
    }

    public void patch(String name, ProviderType type, String baseUrl, String environment) {
        this.name = Objects.requireNonNullElse(name, this.name);
        this.type = Objects.requireNonNullElse(type, this.type);
        this.baseUrl = Objects.requireNonNullElse(baseUrl, this.baseUrl);
        this.environment = Objects.requireNonNullElse(environment, this.environment);
        this.updatedAt = Instant.now();
    }

    public void enable() {
        ensureNotDecommissioned();
        this.status = ProviderStatus.ACTIVE;
        this.updatedAt = Instant.now();
    }

    public void disable() {
        ensureNotDecommissioned();
        this.status = ProviderStatus.DISABLED;
        this.updatedAt = Instant.now();
    }

    public void decommission() {
        this.status = ProviderStatus.DECOMMISSIONED;
        this.updatedAt = Instant.now();
    }

    private void ensureNotDecommissioned() {
        if (status == ProviderStatus.DECOMMISSIONED) {
            throw new IllegalStateException("Decommissioned providers cannot change lifecycle state");
        }
    }
}
