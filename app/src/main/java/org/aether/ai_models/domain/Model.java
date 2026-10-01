package org.aether.ai_models.domain;

import jakarta.persistence.*;
import org.aether.provider.domain.Provider;

import java.time.Instant;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "models", uniqueConstraints = {
        @UniqueConstraint(name = "uk_model_provider_model_id", columnNames = { "provider_id", "provider_model_id" })
}, indexes = {
        @Index(name = "idx_models_provider_id", columnList = "provider_id")
})
public class Model {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "provider_id", nullable = false, foreignKey = @ForeignKey(name = "fk_model_provider"))
    private Provider provider;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "provider_model_id", nullable = false, length = 255)
    private String providerModelId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ModelStatus status;

    @Column(name = "context_window")
    private Integer contextWindow;

    @Column(name = "max_output_tokens")
    private Integer maxOutputTokens;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "model_capabilities", joinColumns = @JoinColumn(name = "model_id", nullable = false, foreignKey = @ForeignKey(name = "fk_model_capability_model")))
    @Enumerated(EnumType.STRING)
    @Column(name = "capability", nullable = false, length = 50)
    private Set<ModelCapability> capabilities = new HashSet<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /*
     * Required by JPA.
     */
    protected Model() {
    }

    public Model(
            Provider provider,
            String name,
            String providerModelId,
            Integer contextWindow,
            Integer maxOutputTokens,
            Set<ModelCapability> capabilities) {

        this.provider = Objects.requireNonNull(
                provider,
                "Provider must not be null");

        this.name = requireText(
                name,
                "Model name must not be blank");

        this.providerModelId = requireText(
                providerModelId,
                "Provider model ID must not be blank");

        validateTokenLimits(
                contextWindow,
                maxOutputTokens);

        Instant now = Instant.now();

        this.id = UUID.randomUUID();
        this.contextWindow = contextWindow;
        this.maxOutputTokens = maxOutputTokens;

        this.capabilities = capabilities == null
                ? new HashSet<>()
                : new HashSet<>(capabilities);

        this.status = ModelStatus.ACTIVE;
        this.createdAt = now;
        this.updatedAt = now;
    }

    public UUID getId() {
        return id;
    }

    public Provider getProvider() {
        return provider;
    }

    public String getName() {
        return name;
    }

    public String getProviderModelId() {
        return providerModelId;
    }

    public ModelStatus getStatus() {
        return status;
    }

    public Integer getContextWindow() {
        return contextWindow;
    }

    public Integer getMaxOutputTokens() {
        return maxOutputTokens;
    }

    public Set<ModelCapability> getCapabilities() {
        return Set.copyOf(capabilities);
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    /*
     * Domain behaviour
     */

    public void enable() {

        ensureNotDecommissioned();

        if (status == ModelStatus.ACTIVE) {
            return;
        }

        this.status = ModelStatus.ACTIVE;
        touch();
    }

    public void disable() {

        ensureNotDecommissioned();

        if (status == ModelStatus.DISABLED) {
            return;
        }

        this.status = ModelStatus.DISABLED;
        touch();
    }

    public void update(String name, String providerModelId, Integer contextWindow,
                       Integer maxOutputTokens, Set<ModelCapability> capabilities) {
        String nextName = requireText(name, "Model name must not be blank");
        String nextProviderModelId = requireText(providerModelId, "Provider model ID must not be blank");
        validateTokenLimits(contextWindow, maxOutputTokens);
        this.name = nextName;
        this.providerModelId = nextProviderModelId;
        this.contextWindow = contextWindow;
        this.maxOutputTokens = maxOutputTokens;
        this.capabilities = capabilities == null ? new HashSet<>() : new HashSet<>(capabilities);
        touch();
    }

    public void patch(String name, String providerModelId, Integer contextWindow,
                      Integer maxOutputTokens, Set<ModelCapability> capabilities) {
        String nextName = name == null ? this.name : requireText(name, "Model name must not be blank");
        String nextProviderModelId = providerModelId == null
                ? this.providerModelId : requireText(providerModelId, "Provider model ID must not be blank");
        Integer nextContextWindow = contextWindow == null ? this.contextWindow : contextWindow;
        Integer nextMaxOutputTokens = maxOutputTokens == null ? this.maxOutputTokens : maxOutputTokens;
        validateTokenLimits(nextContextWindow, nextMaxOutputTokens);
        this.name = nextName;
        this.providerModelId = nextProviderModelId;
        this.contextWindow = nextContextWindow;
        this.maxOutputTokens = nextMaxOutputTokens;
        if (capabilities != null) {
            this.capabilities = new HashSet<>(capabilities);
        }
        touch();
    }

    public void decommission() {
        this.status = ModelStatus.DECOMMISSIONED;
        touch();
    }

    public boolean supports(ModelCapability capability) {
        return capabilities.contains(capability);
    }

    public void addCapability(ModelCapability capability) {

        Objects.requireNonNull(
                capability,
                "Capability must not be null");

        if (capabilities.add(capability)) {
            touch();
        }
    }

    public void removeCapability(ModelCapability capability) {

        if (capability != null && capabilities.remove(capability)) {
            touch();
        }
    }

    /*
     * Internal helpers
     */

    private void touch() {
        this.updatedAt = Instant.now();
    }

    private void ensureNotDecommissioned() {
        if (status == ModelStatus.DECOMMISSIONED) {
            throw new IllegalStateException("Decommissioned models cannot change lifecycle state");
        }
    }

    private static String requireText(
            String value,
            String message) {

        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }

        return value.trim();
    }

    private static void validateTokenLimits(
            Integer contextWindow,
            Integer maxOutputTokens) {

        if (contextWindow != null && contextWindow <= 0) {
            throw new IllegalArgumentException(
                    "Context window must be greater than zero");
        }

        if (maxOutputTokens != null && maxOutputTokens <= 0) {
            throw new IllegalArgumentException(
                    "Max output tokens must be greater than zero");
        }

        if (contextWindow != null
                && maxOutputTokens != null
                && maxOutputTokens > contextWindow) {

            throw new IllegalArgumentException(
                    "Max output tokens cannot exceed context window");
        }
    }
}
