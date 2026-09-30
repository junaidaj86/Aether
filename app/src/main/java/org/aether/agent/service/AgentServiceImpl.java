package org.aether.agent.service;
import org.aether.agent.domain.AgentStatus;
import org.aether.agent.api.AgentResponse;
import org.aether.agent.api.AgentRequest;
import org.aether.agent.domain.Agent;
import java.util.UUID;
import java.time.Instant;
import org.aether.agent.repository.AgentRepository;
import org.common.exception.AgentNotFoundException;
import org.springframework.stereotype.Service;
import java.util.List;

@Service 
public class AgentServiceImpl implements AgentService {

    private final AgentRepository agentDAO;

    public AgentServiceImpl(AgentRepository agentDAO) {
        this.agentDAO = agentDAO;
    }

    @Override
    public AgentResponse registerAgent(AgentRequest request) {

        Agent agent = new Agent(
            UUID.randomUUID(), 
            request.name(), 
            request.description(), 
            request.owner(),
            request.team(), 
            request.environment(), 
            request.riskLevel(), 
            AgentStatus.REGISTERED, 
            Instant.now(), 
            Instant.now()
        );
        agentDAO.save(agent);
        return toResponse(agent);
    }

    @Override
    public AgentResponse getAgentById(UUID id) {
        Agent agent = agentDAO.findById(id).orElseThrow(() -> new AgentNotFoundException(id));
        return toResponse(agent);
    }

    @Override
    public AgentResponse updateAgent(UUID id, AgentRequest request) {
        Agent agent = agentDAO.findById(id).orElseThrow(() -> new AgentNotFoundException(id));
        Agent updatedAgent = agent.update(
                request.name(),
                request.description(),
                request.owner(),
                request.team(),
                request.environment(),
                request.riskLevel());
        agentDAO.update(updatedAgent);
        return toResponse(updatedAgent);
    }

    @Override
    public void deleteAgent(UUID id) {
        Agent existingAgent = agentDAO.findById(id).orElseThrow(() -> new AgentNotFoundException(id));
        agentDAO.delete(existingAgent);
    }

    @Override
    public AgentResponse getAgentByName(String name) {
        Agent agent = agentDAO.findByName(name).orElseThrow(() -> new AgentNotFoundException(UUID.randomUUID()));
        return toResponse(agent);
    }

    @Override
    public List<AgentResponse> getAllAgents() {
        List<Agent> agents = agentDAO.findAll();
        return agents.stream().map(this::toResponse).toList();
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
                agent.getCreatedAt(),
                agent.getUpdatedAt()
        );
    }
}
