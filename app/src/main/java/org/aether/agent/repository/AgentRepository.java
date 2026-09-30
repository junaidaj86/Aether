package org.aether.agent.repository;
import java.util.UUID;
import org.aether.agent.domain.Agent;
import java.util.Optional;
import java.util.List;

public interface AgentRepository {
    void save(Agent agent);

    Optional<Agent> findById(UUID id);

    void delete(Agent agent);

    Agent update(Agent agent);

    Optional<Agent> findByName(String name);

    List<Agent> findAll();
}
