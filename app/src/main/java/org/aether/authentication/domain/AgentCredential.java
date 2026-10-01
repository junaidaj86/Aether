package org.aether.authentication.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.aether.agent.domain.Agent;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "agent_credentials",
        indexes = {
                @Index(
                        name = "idx_agent_credentials_client_id",
                        columnList = "client_id",
                        unique = true
                )
        }
)
@Getter
@NoArgsConstructor
public class AgentCredential {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "agent_id", nullable = false)
    private Agent agent;

    @Column(name = "client_id", nullable = false, unique = true, updatable = false)
    private String clientId;

    @Column(name = "secret_hash", nullable = false)
    private String secretHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CredentialStatus status;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant expiresAt;

    @Column
    private Instant lastUsedAt;

    public AgentCredential(
            Agent agent,
            String clientId,
            String secretHash,
            CredentialStatus status,
            Instant createdAt,
            Instant expiresAt) {

        this.agent = agent;
        this.clientId = clientId;
        this.secretHash = secretHash;
        this.status = status;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
    }

    public void markUsed() {
        this.lastUsedAt = Instant.now();
    }

    public void revoke() {
        this.status = CredentialStatus.REVOKED;
    }

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    public boolean isActive() {
        return status == CredentialStatus.ACTIVE && !isExpired();
    }
}