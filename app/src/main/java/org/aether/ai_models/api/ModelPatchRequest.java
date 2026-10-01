package org.aether.ai_models.api;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.aether.ai_models.domain.ModelCapability;

import java.util.Set;

public record ModelPatchRequest(
        @Size(max = 100) @Pattern(regexp = ".*\\S.*") String name,
        @Size(max = 255) @Pattern(regexp = ".*\\S.*") String providerModelId,
        @Positive Integer contextWindow,
        @Positive Integer maxOutputTokens,
        Set<ModelCapability> capabilities
) {}
