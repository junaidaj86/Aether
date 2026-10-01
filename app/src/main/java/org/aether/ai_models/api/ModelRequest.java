package org.aether.ai_models.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.aether.ai_models.domain.ModelCapability;

import java.util.Set;

public record ModelRequest(

        @NotBlank
        @Size(max = 100)
        String name,

        @NotBlank
        @Size(max = 255)
        String providerModelId,

        @Positive
        Integer contextWindow,

        @Positive
        Integer maxOutputTokens,

        Set<ModelCapability> capabilities

) {
}