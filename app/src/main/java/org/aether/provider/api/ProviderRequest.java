package org.aether.provider.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.aether.provider.domain.ProviderType;

public record ProviderRequest(

        @NotBlank
        @Size(max = 100)
        String name,

        @NotNull
        ProviderType type,

        @NotBlank
        @Size(max = 500)
        String baseUrl,

        @NotBlank
        @Size(max = 50)
        String environment
) {
}