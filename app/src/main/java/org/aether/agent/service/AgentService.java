package org.aether.agent.service;
import java.util.List;
import java.util.UUID;
import org.aether.agent.api.AgentRequest;
import org.aether.agent.api.AgentResponse;
import org.aether.agent.api.AgentPatchRequest;
import org.aether.agent.domain.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AgentService {
    AgentResponse registerAgent(AgentRequest request);
    AgentResponse getAgentById(UUID id);
    AgentResponse updateAgent(UUID id, AgentRequest request);
    AgentResponse patchAgent(UUID id, AgentPatchRequest request);
    void deleteAgent(UUID id);
    AgentResponse activateAgent(UUID id);
    AgentResponse deactivateAgent(UUID id);
    List<AgentResponse> getAgentsByName(String name, Environment environment);
    Page<AgentResponse> getAllAgents(Pageable pageable);
}
