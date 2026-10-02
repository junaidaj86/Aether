package org.aether.agent.api;

import org.aether.agent.domain.AgentStatus;
import org.aether.agent.domain.Environment;
import org.aether.agent.domain.RiskLevel;
import org.aether.agent.service.AgentService;
import org.aether.agent.exception.AgentNotFoundException;
import org.aether.common.error.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AgentController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class AgentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AgentService agentService;

    private final UUID id = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Test
    void patchEndpointReturnsUpdatedAgent() throws Exception {
        when(agentService.patchAgent(eq(id), any(AgentPatchRequest.class)))
                .thenReturn(response(AgentStatus.REGISTERED));

        mockMvc.perform(patch("/api/v1/agent").param("id", id.toString())
                        .header("X-Correlation-ID", "client-request-123")
                        .header("X-Trace-ID", "trace-123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\":\"updated description\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REGISTERED"));
    }

    @Test
    void deleteEndpointReturnsNoContentAndDelegatesToService() throws Exception {
        mockMvc.perform(delete("/api/v1/agent").param("id", id.toString()))
                .andExpect(status().isNoContent());

        verify(agentService).deleteAgent(id);
    }

    @Test
    void lifecycleEndpointsDelegateToService() throws Exception {
        when(agentService.activateAgent(id)).thenReturn(response(AgentStatus.ACTIVE));
        when(agentService.deactivateAgent(id)).thenReturn(response(AgentStatus.INACTIVE));

        mockMvc.perform(post("/api/v1/agent/{id}/activate", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        mockMvc.perform(post("/api/v1/agent/{id}/deactivate", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("INACTIVE"));
    }

    @Test
    void missingAgentReturnsNotFound() throws Exception {
        when(agentService.getAgentById(id)).thenThrow(new AgentNotFoundException(id));

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .get("/api/v1/agent/{id}", id)
                        .header("X-Correlation-ID", "missing-agent-123"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("AGENT_NOT_FOUND"));
    }

    @Test
    void blankPatchFieldIsRejected() throws Exception {
        mockMvc.perform(patch("/api/v1/agent").param("id", id.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"owner\":\"   \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    private AgentResponse response(AgentStatus status) {
        return new AgentResponse(id, "jira-agent", "description", "owner", "team",
                Environment.DEVELOPMENT, RiskLevel.MEDIUM, status, "entra", "principal",
                Instant.parse("2026-01-01T00:00:00Z"), Instant.parse("2026-01-01T00:00:00Z"));
    }
}
