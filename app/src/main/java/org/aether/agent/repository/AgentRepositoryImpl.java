package org.aether.agent.repository;
import java.util.UUID;
import org.aether.agent.domain.Agent;
import org.springframework.stereotype.Repository;
import java.util.List;

import java.util.Optional;

@Repository 
public class AgentRepositoryImpl implements AgentRepository {
    @Override
    public void save(Agent agent) {
        // Implementation here
    }

    @Override
    public Optional<Agent> findById(UUID id) {
        // Implementation here
        return Optional.empty();
    }

    @Override
    public void delete(Agent agent) {
        // Implementation here
    }

    @Override
    public Agent update(Agent agent) {
        // Implementation here
        return null;
    }

    @Override
    public Optional<Agent> findByName(String name) {
        // Implementation here
        return Optional.empty();
    }

    @Override
    public List<Agent> findAll() {
        // Implementation here
        return List.of();
    }
}
