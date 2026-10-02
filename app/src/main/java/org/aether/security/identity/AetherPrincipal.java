package org.aether.security.identity;

import java.util.Map;
import java.util.Set;

public record AetherPrincipal(
        String issuer,
        IdentityProvider identityProvider,
        String externalPrincipalId,
        String displayName,
        Set<String> roles,
        Map<String, Object> attributes
) {
    public AetherPrincipal {
        roles = roles == null ? Set.of() : Set.copyOf(roles);
        attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
    }
}
