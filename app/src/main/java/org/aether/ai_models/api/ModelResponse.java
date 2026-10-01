package org.aether.ai_models.api;

import org.aether.ai_models.domain.ModelCapability;
import org.aether.ai_models.domain.ModelStatus;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record ModelResponse(

        UUID id,

        UUID providerId,
        String providerName,

        String name,
        String providerModelId,

        ModelStatus status,

        Integer contextWindow,
        Integer maxOutputTokens,

        Set<ModelCapability> capabilities,

        Instant createdAt,
        Instant updatedAt

) {
}