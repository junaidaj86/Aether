package org.aether.agent.api;

import org.aether.agent.domain.Environment;
import org.aether.agent.domain.RiskLevel;

import jakarta.validation.constraints.Size;

public record AgentPatchRequest(
        @Size(min = 3, max = 100, message = "Agent name must be between 3 and 100 characters")
        String name,

        @Size(max = 500, message = "Description must not exceed 500 characters")
        String description,

        @Size(max = 100, message = "Owner must not exceed 100 characters")
        String owner,

        @Size(max = 100, message = "Team must not exceed 100 characters")
        String team,

        Environment environment,
        RiskLevel riskLevel,

        @Size(max = 100, message = "Identity provider must not exceed 100 characters")
        String identityProvider,

        @Size(max = 255, message = "External principal ID must not exceed 255 characters")
        String externalPrincipalId
) {}
