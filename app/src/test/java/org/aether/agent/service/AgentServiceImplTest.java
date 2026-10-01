package org.aether.agent.service;

import org.aether.agent.api.AgentResponse;
import org.aether.agent.api.AgentPatchRequest;
import org.aether.agent.domain.Agent;
import org.aether.agent.domain.AgentStatus;
import org.aether.agent.domain.Environment;
import org.aether.agent.domain.RiskLevel;
import org.aether.agent.repository.AgentRepository;
import org.aether.agent.exception.AgentNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

class AgentServiceImplTest {

    private final AgentRepository repository = mock(AgentRepository.class);
    private final AgentServiceImpl service = new AgentServiceImpl(repository);

    @Test
    void findsAllEnvironmentsWhenEnvironmentIsNotSpecified() {
        Agent development = agent("jira-agent", Environment.DEVELOPMENT);
        Agent production = agent("jira-agent", Environment.PRODUCTION);
        when(repository.findAllByNameAndStatusNotOrderByEnvironment("jira-agent", AgentStatus.DECOMMISSIONED))
                .thenReturn(List.of(development, production));

        List<AgentResponse> result = service.getAgentsByName("jira-agent", null);

        assertEquals(List.of(Environment.DEVELOPMENT, Environment.PRODUCTION),
                result.stream().map(AgentResponse::environment).toList());
    }

    @Test
    void findsOneEnvironmentWhenEnvironmentIsSpecified() {
        Agent production = agent("jira-agent", Environment.PRODUCTION);
        when(repository.findByNameAndEnvironmentAndStatusNot(
                "jira-agent", Environment.PRODUCTION, AgentStatus.DECOMMISSIONED))
                .thenReturn(Optional.of(production));

        List<AgentResponse> result = service.getAgentsByName("jira-agent", Environment.PRODUCTION);

        assertEquals(1, result.size());
        assertEquals(Environment.PRODUCTION, result.getFirst().environment());
    }

    @Test
    void throwsNotFoundWhenNameDoesNotExist() {
        when(repository.findAllByNameAndStatusNotOrderByEnvironment("missing", AgentStatus.DECOMMISSIONED))
                .thenReturn(List.of());

        assertThrows(AgentNotFoundException.class,
                () -> service.getAgentsByName("missing", null));
    }

    @Test
    void delegatesPaginationToRepository() {
        PageRequest pageable = PageRequest.of(1, 2);
        when(repository.findAllByStatusNot(AgentStatus.DECOMMISSIONED, pageable))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));

        assertEquals(0, service.getAllAgents(pageable).getTotalElements());
    }

    @Test
    void patchChangesOnlySuppliedFields() {
        Agent existing = agent("jira-agent", Environment.DEVELOPMENT);
        when(repository.findById(java.util.UUID.fromString("00000000-0000-0000-0000-000000000001")))
                .thenReturn(Optional.of(existing));

        AgentResponse result = service.patchAgent(
                java.util.UUID.fromString("00000000-0000-0000-0000-000000000001"),
                new AgentPatchRequest(null, "updated description", null, null, null, null, null, null));

        assertEquals("jira-agent", result.name());
        assertEquals("updated description", result.description());
        assertEquals(Environment.DEVELOPMENT, result.environment());
    }

    @Test
    void deleteDecommissionsInsteadOfRemovingAgent() {
        Agent existing = agent("jira-agent", Environment.DEVELOPMENT);
        var id = java.util.UUID.fromString("00000000-0000-0000-0000-000000000001");
        when(repository.findById(id)).thenReturn(Optional.of(existing));

        service.deleteAgent(id);

        assertEquals(AgentStatus.DECOMMISSIONED, existing.getStatus());
        verify(repository, org.mockito.Mockito.never()).delete(existing);
    }

    @Test
    void activatesAndDeactivatesAgent() {
        Agent existing = agent("jira-agent", Environment.DEVELOPMENT);
        var id = java.util.UUID.fromString("00000000-0000-0000-0000-000000000001");
        when(repository.findById(id)).thenReturn(Optional.of(existing));

        assertEquals(AgentStatus.ACTIVE, service.activateAgent(id).status());
        assertEquals(AgentStatus.INACTIVE, service.deactivateAgent(id).status());
    }

    private Agent agent(String name, Environment environment) {
        return new Agent(name, "description", "owner", "team", environment,
                RiskLevel.MEDIUM, AgentStatus.REGISTERED, "entra", "principal",
                "SYSTEM", Instant.parse("2026-01-01T00:00:00Z"));
    }
}
