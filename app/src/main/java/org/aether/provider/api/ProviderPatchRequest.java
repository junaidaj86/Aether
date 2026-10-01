package org.aether.provider.api;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.aether.provider.domain.ProviderType;

public record ProviderPatchRequest(
        @Size(max = 100) @Pattern(regexp = ".*\\S.*") String name,
        ProviderType type,
        @Size(max = 500) @Pattern(regexp = ".*\\S.*") String baseUrl,
        @Size(max = 50) @Pattern(regexp = ".*\\S.*") String environment
) {}
