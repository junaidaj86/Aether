package org.aether.agent.service;
import java.util.List;
import java.util.UUID;
import org.aether.agent.api.AgentRequest;
import org.aether.agent.api.AgentResponse;
import org.springframework.stereotype.Service;

@Service 
public interface AgentService {
    AgentResponse registerAgent(AgentRequest request);
    AgentResponse getAgentById(UUID id);
    AgentResponse updateAgent(UUID id, AgentRequest request);
    void deleteAgent(UUID id);
    AgentResponse getAgentByName(String name);
    List<AgentResponse> getAllAgents();
}
