package org.aether.agent.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "agents",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_agent_name_environment",
                        columnNames = {
                                "name",
                                "environment"
                        }
                ),
                @UniqueConstraint(
                        name = "uk_agent_external_identity",
                        columnNames = {
                                "identity_provider",
                                "external_principal_id"
                        }
                )
        }
)
@Getter
@NoArgsConstructor
public class Agent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /*
     * Logical name of the agent.
     *
     * The same name can exist in different environments.
     *
     * Example:
     * jira-agent / DEVELOPMENT
     * jira-agent / TESTING
     * jira-agent / PRODUCTION
     */
    @Column(nullable = false)
    private String name;

    @Column(nullable = false, length = 500)
    private String description;

    @Column(nullable = false)
    private String owner;

    @Column(nullable = false)
    private String team;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Environment environment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RiskLevel riskLevel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AgentStatus status;

    /*
     * Logical name of the external Identity Provider configuration.
     *
     * Example:
     * scania-entra
     *
     * The actual issuer URI, audience and JWKS configuration
     * are managed centrally in application.yaml.
     */
    @Column(name = "identity_provider", nullable = false)
    private String identityProvider;

    /*
     * Identifier of this agent in the external Identity Provider.
     *
     * For example, this can represent the application/workload
     * identity used in Microsoft Entra.
     *
     * Together with identityProvider, this uniquely identifies
     * the external identity.
     */
    @Column(name = "external_principal_id", nullable = false)
    private String externalPrincipalId;

    /*
     * Principal that registered the agent.
     *
     * This should come from the authenticated caller and
     * should never be supplied by the client in AgentRequest.
     */
    @Column(name = "created_by", nullable = false, updatable = false)
    private String createdBy;

    /*
     * Principal that most recently modified the agent.
     */
    @Column(name = "updated_by", nullable = false)
    private String updatedBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;


    /*
     * Constructor used when registering a new agent.
     */
    public Agent(
            String name,
            String description,
            String owner,
            String team,
            Environment environment,
            RiskLevel riskLevel,
            AgentStatus status,
            String identityProvider,
            String externalPrincipalId,
            String createdBy,
            Instant createdAt) {

        this.name = name;
        this.description = description;
        this.owner = owner;
        this.team = team;
        this.environment = environment;
        this.riskLevel = riskLevel;
        this.status = status;
        this.identityProvider = identityProvider;
        this.externalPrincipalId = externalPrincipalId;
        this.createdBy = createdBy;
        this.updatedBy = createdBy;
        this.createdAt = createdAt;
        this.updatedAt = createdAt;
    }


    /*
     * Update mutable agent information.
     *
     * Identity of the creator and creation timestamp
     * cannot be changed.
     */
    public Agent update(
            String name,
            String description,
            String owner,
            String team,
            Environment environment,
            RiskLevel riskLevel,
            String identityProvider,
            String externalPrincipalId,
            String updatedBy) {

        this.name = name;
        this.description = description;
        this.owner = owner;
        this.team = team;
        this.environment = environment;
        this.riskLevel = riskLevel;
        this.identityProvider = identityProvider;
        this.externalPrincipalId = externalPrincipalId;
        this.updatedBy = updatedBy;
        this.updatedAt = Instant.now();

        return this;
    }


    /*
     * Agent lifecycle operations.
     */
    public void activate(String updatedBy) {
        this.status = AgentStatus.ACTIVE;
        this.updatedBy = updatedBy;
        this.updatedAt = Instant.now();
    }

    public void deactivate(String updatedBy) {
        this.status = AgentStatus.INACTIVE;
        this.updatedBy = updatedBy;
        this.updatedAt = Instant.now();
    }
}