package org.aether.agent.service;

import org.aether.agent.api.AgentResponse;
import org.aether.agent.domain.Agent;
import org.aether.agent.domain.AgentStatus;
import org.aether.agent.domain.Environment;
import org.aether.agent.domain.RiskLevel;
import org.aether.agent.repository.AgentRepository;
import org.common.exception.AgentNotFoundException;
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

class AgentServiceImplTest {

    private final AgentRepository repository = mock(AgentRepository.class);
    private final AgentServiceImpl service = new AgentServiceImpl(repository);

    @Test
    void findsAllEnvironmentsWhenEnvironmentIsNotSpecified() {
        Agent development = agent("jira-agent", Environment.DEVELOPMENT);
        Agent production = agent("jira-agent", Environment.PRODUCTION);
        when(repository.findAllByNameOrderByEnvironment("jira-agent"))
                .thenReturn(List.of(development, production));

        List<AgentResponse> result = service.getAgentsByName("jira-agent", null);

        assertEquals(List.of(Environment.DEVELOPMENT, Environment.PRODUCTION),
                result.stream().map(AgentResponse::environment).toList());
    }

    @Test
    void findsOneEnvironmentWhenEnvironmentIsSpecified() {
        Agent production = agent("jira-agent", Environment.PRODUCTION);
        when(repository.findByNameAndEnvironment("jira-agent", Environment.PRODUCTION))
                .thenReturn(Optional.of(production));

        List<AgentResponse> result = service.getAgentsByName("jira-agent", Environment.PRODUCTION);

        assertEquals(1, result.size());
        assertEquals(Environment.PRODUCTION, result.getFirst().environment());
    }

    @Test
    void throwsNotFoundWhenNameDoesNotExist() {
        when(repository.findAllByNameOrderByEnvironment("missing")).thenReturn(List.of());

        assertThrows(AgentNotFoundException.class,
                () -> service.getAgentsByName("missing", null));
    }

    @Test
    void delegatesPaginationToRepository() {
        PageRequest pageable = PageRequest.of(1, 2);
        when(repository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(), pageable, 0));

        assertEquals(0, service.getAllAgents(pageable).getTotalElements());
    }

    private Agent agent(String name, Environment environment) {
        return new Agent(name, "description", "owner", "team", environment,
                RiskLevel.MEDIUM, AgentStatus.REGISTERED, "entra", "principal",
                "SYSTEM", Instant.parse("2026-01-01T00:00:00Z"));
    }
}
