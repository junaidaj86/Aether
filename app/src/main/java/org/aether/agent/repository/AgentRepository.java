package org.aether.agent.repository;

import org.aether.agent.domain.Agent;
import org.aether.agent.domain.AgentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.aether.agent.domain.Environment;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface AgentRepository extends JpaRepository<Agent, UUID> {
    List<Agent> findAllByNameAndStatusNotOrderByEnvironment(String name, AgentStatus status);

    Optional<Agent> findByNameAndEnvironmentAndStatusNot(String name, Environment environment, AgentStatus status);

    Page<Agent> findAllByStatusNot(AgentStatus status, Pageable pageable);

    boolean existsByNameAndEnvironment(String name, Environment environment);

    boolean existsByIdentityProviderAndExternalPrincipalId(
            String identityProvider, String externalPrincipalId);

    Optional<Agent> findByIdentityProviderAndExternalPrincipalId(
            String identityProvider, String externalPrincipalId);
    
    // create method findAllByEnvironment
    List<Agent> findAllByEnvironment(Environment environment);
}
