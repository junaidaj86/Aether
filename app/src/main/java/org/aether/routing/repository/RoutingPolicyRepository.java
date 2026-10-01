package org.aether.routing.repository;

import org.aether.routing.domain.RoutingPolicy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RoutingPolicyRepository extends JpaRepository<RoutingPolicy, UUID> {
    List<RoutingPolicy> findByAgentIdAndEnabledTrue(UUID agentId);
}
