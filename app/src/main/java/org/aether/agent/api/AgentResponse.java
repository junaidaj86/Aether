package org.aether.agent.api;
import java.time.Instant;
import java.util.UUID;

import org.aether.agent.domain.AgentStatus;
import org.aether.agent.domain.Environment;
import org.aether.agent.domain.RiskLevel;

public record AgentResponse(
        UUID id,

        String name,
        
        String description,

        String owner,

        String team,

        Environment environment,

        RiskLevel riskLevel,

        AgentStatus status,

        Instant createdAt,

        Instant updatedAt

) {
    
}
