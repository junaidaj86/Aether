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

        @NotBlank(message = "Description is required")
        @Size(max = 500,
              message = "Description must not exceed 500 characters")
        String description,

        @NotBlank(message = "Owner is required")
        @Size(max = 100, message = "Owner must not exceed 100 characters")
        String owner,

        @NotBlank(message = "Team is required")
        @Size(max = 100, message = "Team must not exceed 100 characters")
        String team,

        @NotNull(message = "Environment is required")
        Environment environment,

        @NotNull(message = "Risk level is required")
        RiskLevel riskLevel,

        @NotBlank(message = "Identity provider is required")
        @Size(max = 100, message = "Identity provider must not exceed 100 characters")
        String identityProvider,

        @NotBlank(message = "External principal ID is required")
        @Size(max = 255, message = "External principal ID must not exceed 255 characters")
        String externalPrincipalId

) {}
