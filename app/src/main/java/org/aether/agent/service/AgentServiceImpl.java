package org.aether.agent.service;

import org.aether.agent.api.AgentRequest;
import org.aether.agent.api.AgentResponse;
import org.aether.agent.api.AgentPatchRequest;
import org.aether.agent.domain.Agent;
import org.aether.agent.domain.AgentStatus;
import org.aether.agent.domain.Environment;
import org.aether.agent.repository.AgentRepository;
import org.aether.agent.exception.AgentNotFoundException;
import org.aether.agent.exception.AgentAlreadyExistsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Transactional(readOnly = true)
@Service
public class AgentServiceImpl implements AgentService {

    private static final Logger log = LoggerFactory.getLogger(AgentServiceImpl.class);

    private static final String SYSTEM_PRINCIPAL = "SYSTEM";

    private final AgentRepository agentDAO;

    public AgentServiceImpl(AgentRepository agentDAO) {
        this.agentDAO = agentDAO;
    }

    @Transactional
    @Override
    public AgentResponse registerAgent(AgentRequest request) {

        if (agentDAO.existsByNameAndEnvironment(request.name(), request.environment())) {
            throw new AgentAlreadyExistsException(request.name() + " / " + request.environment());
        }
        if (agentDAO.existsByIdentityProviderAndExternalPrincipalId(
                request.identityProvider(), request.externalPrincipalId())) {
            throw new AgentAlreadyExistsException(
                    request.identityProvider() + " / " + request.externalPrincipalId());
        }

        Instant now = Instant.now();

        Agent agent = new Agent(
                request.name(),
                request.description(),
                request.owner(),
                request.team(),
                request.environment(),
                request.riskLevel(),
                AgentStatus.REGISTERED,
                request.identityProvider(),
                request.externalPrincipalId(),
                SYSTEM_PRINCIPAL,
                now
        );

        agentDAO.save(agent);

        log.info("event=agent_registered agentId={} name={} environment={}",
                agent.getId(), agent.getName(), agent.getEnvironment());

        return toResponse(agent);
    }

    @Override
    public AgentResponse getAgentById(UUID id) {

        Agent agent = agentDAO.findById(id)
                .orElseThrow(() -> new AgentNotFoundException(id));

        ensureVisible(agent, id);

        return toResponse(agent);
    }

    @Transactional
    @Override
    public AgentResponse updateAgent(UUID id, AgentRequest request) {

        Agent agent = agentDAO.findById(id)
                .orElseThrow(() -> new AgentNotFoundException(id));

        ensureVisible(agent, id);

        agent.update(
                request.name(),
                request.description(),
                request.owner(),
                request.team(),
                request.environment(),
                request.riskLevel(),
                request.identityProvider(),
                request.externalPrincipalId(),
                SYSTEM_PRINCIPAL
        );

        log.info("event=agent_updated agentId={} name={} environment={}",
                agent.getId(), agent.getName(), agent.getEnvironment());

        return toResponse(agent);
    }

    @Transactional
    @Override
    public AgentResponse patchAgent(UUID id, AgentPatchRequest request) {
        Agent agent = agentDAO.findById(id)
                .orElseThrow(() -> new AgentNotFoundException(id));

        ensureVisible(agent, id);
        if (request.name() == null && request.description() == null && request.owner() == null
                && request.team() == null && request.environment() == null
                && request.riskLevel() == null && request.identityProvider() == null
                && request.externalPrincipalId() == null) {
            throw new IllegalArgumentException("At least one field must be supplied for patch");
        }

        agent.patch(request.name(), request.description(), request.owner(), request.team(),
                request.environment(), request.riskLevel(), request.identityProvider(),
                request.externalPrincipalId(), SYSTEM_PRINCIPAL);

        log.info("event=agent_patched agentId={} name={} environment={}",
                agent.getId(), agent.getName(), agent.getEnvironment());

        return toResponse(agent);
    }

    @Transactional
    @Override
    public void deleteAgent(UUID id) {

        Agent existingAgent = agentDAO.findById(id)
                .orElseThrow(() -> new AgentNotFoundException(id));

        ensureVisible(existingAgent, id);
        existingAgent.decommission(SYSTEM_PRINCIPAL);

        log.info("event=agent_decommissioned agentId={} name={}",
                existingAgent.getId(), existingAgent.getName());
    }

    @Transactional
    @Override
    public AgentResponse activateAgent(UUID id) {
        Agent agent = findVisible(id);
        agent.activate(SYSTEM_PRINCIPAL);
        log.info("event=agent_activated agentId={} name={}", agent.getId(), agent.getName());
        return toResponse(agent);
    }

    @Transactional
    @Override
    public AgentResponse deactivateAgent(UUID id) {
        Agent agent = findVisible(id);
        agent.deactivate(SYSTEM_PRINCIPAL);
        log.info("event=agent_deactivated agentId={} name={}", agent.getId(), agent.getName());
        return toResponse(agent);
    }

    @Override
    public List<AgentResponse> getAgentsByName(String name, Environment environment) {
        List<Agent> agents = environment == null
                ? agentDAO.findAllByNameAndStatusNotOrderByEnvironment(name, AgentStatus.DECOMMISSIONED)
                : agentDAO.findByNameAndEnvironmentAndStatusNot(name, environment, AgentStatus.DECOMMISSIONED)
                        .stream().toList();

        if (agents.isEmpty()) {
            throw new AgentNotFoundException(name);
        }

        return agents.stream().map(this::toResponse).toList();
    }

    @Override
    public Page<AgentResponse> getAllAgents(Pageable pageable) {
        return agentDAO.findAllByStatusNot(AgentStatus.DECOMMISSIONED, pageable).map(this::toResponse);
    }

    private Agent findVisible(UUID id) {
        Agent agent = agentDAO.findById(id)
                .orElseThrow(() -> new AgentNotFoundException(id));
        ensureVisible(agent, id);
        return agent;
    }

    private void ensureVisible(Agent agent, UUID id) {
        if (agent.getStatus() == AgentStatus.DECOMMISSIONED) {
            throw new AgentNotFoundException(id);
        }
    }

    private AgentResponse toResponse(Agent agent) {

        return new AgentResponse(
                agent.getId(),
                agent.getName(),
                agent.getDescription(),
                agent.getOwner(),
                agent.getTeam(),
                agent.getEnvironment(),
                agent.getRiskLevel(),
                agent.getStatus(),
                agent.getIdentityProvider(),
                agent.getExternalPrincipalId(),
                agent.getCreatedAt(),
                agent.getUpdatedAt()
        );
    }
}
