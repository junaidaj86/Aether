package org.aether.agent.service;

import org.aether.agent.api.AgentRequest;
import org.aether.agent.api.AgentResponse;
import org.aether.agent.domain.Agent;
import org.aether.agent.domain.AgentStatus;
import org.aether.agent.repository.AgentRepository;
import org.common.exception.AgentNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Transactional(readOnly = true)
@Service
public class AgentServiceImpl implements AgentService {

    private static final String SYSTEM_PRINCIPAL = "SYSTEM";

    private final AgentRepository agentDAO;

    public AgentServiceImpl(AgentRepository agentDAO) {
        this.agentDAO = agentDAO;
    }

    @Transactional
    @Override
    public AgentResponse registerAgent(AgentRequest request) {

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

        return toResponse(agent);
    }

    @Override
    public AgentResponse getAgentById(UUID id) {

        Agent agent = agentDAO.findById(id)
                .orElseThrow(() -> new AgentNotFoundException(id));

        return toResponse(agent);
    }

    @Transactional
    @Override
    public AgentResponse updateAgent(UUID id, AgentRequest request) {

        Agent agent = agentDAO.findById(id)
                .orElseThrow(() -> new AgentNotFoundException(id));

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

        return toResponse(agent);
    }

    @Transactional
    @Override
    public void deleteAgent(UUID id) {

        Agent existingAgent = agentDAO.findById(id)
                .orElseThrow(() -> new AgentNotFoundException(id));

        agentDAO.delete(existingAgent);
    }

    @Override
    public AgentResponse getAgentByName(String name) {

        Agent agent = agentDAO.findByName(name)
                .orElseThrow(() -> new AgentNotFoundException(name));

        return toResponse(agent);
    }

    @Override
    public List<AgentResponse> getAllAgents() {

        return agentDAO.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
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