package org.aether.security.authorization;

import org.aether.agent.domain.Agent;
import org.aether.agent.domain.AgentStatus;
import org.aether.agent.exception.AgentNotRegisteredException;
import org.aether.agent.repository.AgentRepository;
import org.aether.security.identity.AetherPrincipal;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;

@Service("authorizationService")
public class AuthorizationService {

    private final AgentRepository agentRepository;

    public AuthorizationService(AgentRepository agentRepository) {
        this.agentRepository = agentRepository;
    }

    public AetherPrincipal currentPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken
                || !(authentication.getPrincipal() instanceof AetherPrincipal principal)) {
            throw new AgentNotRegisteredException();
        }
        return principal;
    }

    public boolean hasAnyRole(String... roles) {
        AetherPrincipal principal = currentPrincipal();
        return Arrays.stream(roles).anyMatch(principal.roles()::contains)
                || principal.roles().contains("aether.admin");
    }

    @Transactional(readOnly = true)
    public Agent requireRegisteredAgent() {
        AetherPrincipal principal = currentPrincipal();
        return agentRepository.findByIdentityProviderAndExternalPrincipalId(
                        principal.identityProvider().name(), principal.externalPrincipalId())
                .filter(agent -> agent.getStatus() != AgentStatus.DECOMMISSIONED)
                .orElseThrow(AgentNotRegisteredException::new);
    }
}
