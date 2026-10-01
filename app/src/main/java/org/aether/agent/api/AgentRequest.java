package org.aether.agent.api;

import org.aether.agent.domain.Environment;
import org.aether.agent.domain.RiskLevel;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AgentRequest(

        @NotBlank(message = "Agent name is required")
        @Size(min = 3, max = 100,
              message = "Agent name must be between 3 and 100 characters")
        String name,

        @Size(max = 500,
              message = "Description must not exceed 500 characters")
        String description,

        @NotBlank(message = "Owner is required")
        String owner,

        @NotBlank(message = "Team is required")
        String team,

        @NotNull(message = "Environment is required")
        Environment environment,

        @NotNull(message = "Risk level is required")
        RiskLevel riskLevel,

        @NotBlank(message = "Identity provider is required")
        String identityProvider,

        @NotBlank(message = "External principal ID is required")
        String externalPrincipalId

) {}