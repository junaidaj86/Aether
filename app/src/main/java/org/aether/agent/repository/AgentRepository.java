package org.aether.agent.repository;

import org.aether.agent.domain.Agent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.aether.agent.domain.Environment;
import java.util.List;
import java.util.Optional;

import java.util.UUID;

public interface AgentRepository extends JpaRepository<Agent, UUID> {
    // crate method findbyname
    Optional<Agent> findByName(String name);
    
    // create method findAllByEnvironment
    List<Agent> findAllByEnvironment(Environment environment);
}