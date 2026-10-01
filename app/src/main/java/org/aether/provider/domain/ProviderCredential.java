package org.aether.provider.domain;

import jakarta.persistence.*;
import org.aether.authentication.domain.CredentialStatus;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "provider_credentials", uniqueConstraints = {
        @UniqueConstraint(name = "uk_provider_credentials_provider_name", columnNames = {"provider_id", "name"})
})
public class ProviderCredential {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "provider_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_provider_credentials_provider"))
    private Provider provider;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "secret_ref", nullable = false, length = 500)
    private String secretRef;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private CredentialStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(name = "last_used_at")
    private Instant lastUsedAt;

    protected ProviderCredential() {
    }

    public ProviderCredential(Provider provider, String name, String secretRef,
                              CredentialStatus status, Instant createdAt, Instant expiresAt) {
        this.provider = provider;
        this.name = requireText(name, "Credential name must not be blank");
        this.secretRef = requireText(secretRef, "Credential secret reference must not be blank");
        this.status = status;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
    }

    public UUID getId() { return id; }
    public Provider getProvider() { return provider; }
    public String getName() { return name; }
    public String getSecretRef() { return secretRef; }
    public CredentialStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getExpiresAt() { return expiresAt; }
    public Instant getLastUsedAt() { return lastUsedAt; }

    public void markUsed() {
        this.lastUsedAt = Instant.now();
    }

    public boolean isUsable() {
        return status == CredentialStatus.ACTIVE
                && (expiresAt == null || Instant.now().isBefore(expiresAt));
    }

    public void revoke() {
        this.status = CredentialStatus.REVOKED;
    }

    private static String requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }
}
