package org.aether.provider.api;

import org.aether.provider.domain.ProviderStatus;
import org.aether.provider.domain.ProviderType;

import java.time.Instant;
import java.util.UUID;

public record ProviderResponse(
        UUID id,
        String name,
        ProviderType type,
        String baseUrl,
        String environment,
        ProviderStatus status,
        Instant createdAt,
        Instant updatedAt
) {
}