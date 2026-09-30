package org.aether.agent.domain;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class Agent {
    private  UUID id;
    private  String name;
    private  String description;
    private  String owner;
    private  String team;
    private  Environment environment;
    private  RiskLevel riskLevel;
    private  AgentStatus status;
    private  Instant createdAt;
    private  Instant updatedAt;

    public Agent update(
        String name,
        String description,
        String owner,
        String team,
        Environment environment,
        RiskLevel riskLevel) {

    this.name = name;
    this.description = description;
    this.owner = owner;
    this.team = team;
    this.environment = environment;
    this.riskLevel = riskLevel;
    this.updatedAt = Instant.now();
    return this;
}
}


