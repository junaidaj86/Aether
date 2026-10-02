package org.aether.security.config;

import jakarta.servlet.http.HttpServletRequest;
import org.aether.agent.service.AgentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationManagerResolver;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "aether.security.enabled=true",
        "aether.security.issuers[0].name=test",
        "aether.security.issuers[0].provider=GENERIC_OIDC",
        "aether.security.issuers[0].issuer-uri=https://issuer.example.test",
        "aether.security.issuers[0].audience=aether-api",
        "aether.security.issuers[0].principal-claim=sub"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AgentService agentService;

    @MockitoBean(name = "authenticationManagerResolver")
    private AuthenticationManagerResolver<HttpServletRequest> authenticationManagerResolver;

    @BeforeEach
    void configureAuthenticationManager() {
        AuthenticationManager manager = authentication ->
                new TestingAuthenticationToken(
                        "test-client", "[PROTECTED]", "ROLE_aether.registry.read");
        when(authenticationManagerResolver.resolve(any(HttpServletRequest.class)))
                .thenReturn(manager);
        when(agentService.getAllAgents(any())).thenReturn(Page.empty());
    }

    @Test
    void rejectsMissingAuthorizationHeaderWithCorrelationId() throws Exception {
        mockMvc.perform(get("/api/v1/agent")
                        .header("X-Correlation-ID", "security-test-123"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string("X-Correlation-ID", "security-test-123"))
                .andExpect(jsonPath("$.code").value("AUTHENTICATION_REQUIRED"))
                .andExpect(jsonPath("$.correlationId").value("security-test-123"));
    }

    @Test
    void permitsHealthWithoutAuthorization() throws Exception {
        mockMvc.perform(get("/actuator/health/liveness"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void acceptsAuthenticatedRequestWithNormalizedAuthority() throws Exception {
        mockMvc.perform(get("/api/v1/agent")
                        .header("Authorization", "Bearer test-token")
                        .header("X-Correlation-ID", "security-test-456"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Correlation-ID", "security-test-456"));
    }

    @Test
    void deniesWriteRequestWhenOnlyReadAuthorityIsPresent() throws Exception {
        mockMvc.perform(post("/api/v1/agent")
                        .header("Authorization", "Bearer test-token")
                        .header("X-Correlation-ID", "security-test-789"))
                .andExpect(status().isForbidden())
                .andExpect(header().string("X-Correlation-ID", "security-test-789"))
                .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));
    }
}
