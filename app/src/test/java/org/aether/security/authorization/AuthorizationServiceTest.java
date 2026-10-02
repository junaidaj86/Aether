package org.aether.security.authorization;

import org.aether.agent.domain.Agent;
import org.aether.agent.domain.AgentStatus;
import org.aether.agent.domain.Environment;
import org.aether.agent.domain.RiskLevel;
import org.aether.agent.repository.AgentRepository;
import org.aether.security.identity.AetherAuthenticationToken;
import org.aether.security.identity.AetherPrincipal;
import org.aether.security.identity.IdentityProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthorizationServiceTest {

    @Mock
    private AgentRepository agentRepository;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void resolvesRegisteredAgentFromNormalizedIdentity() {
        AuthorizationService service = new AuthorizationService(agentRepository);
        AetherPrincipal principal = new AetherPrincipal(
                "https://issuer.example.test",
                IdentityProvider.KEYCLOAK,
                "agent-123",
                "agent-client",
                java.util.Set.of("aether.inference"),
                Map.of());
        setAuthentication(principal);

        Agent agent = new Agent("jira-agent", "desc", "owner", "team",
                Environment.DEVELOPMENT, RiskLevel.MEDIUM, AgentStatus.REGISTERED,
                "KEYCLOAK", "agent-123", "SYSTEM", Instant.now());
        when(agentRepository.findByIdentityProviderAndExternalPrincipalId(
                "KEYCLOAK", "agent-123")).thenReturn(Optional.of(agent));

        assertThat(service.requireRegisteredAgent()).isSameAs(agent);
        assertThat(service.hasAnyRole("aether.inference")).isTrue();
    }

    private void setAuthentication(AetherPrincipal principal) {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "RS256")
                .issuer(principal.issuer())
                .subject(principal.externalPrincipalId())
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(300))
                .build();
        SecurityContextHolder.getContext().setAuthentication(
                new AetherAuthenticationToken(principal, jwt, List.of()));
    }
}
