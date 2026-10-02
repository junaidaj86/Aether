package org.aether.security.identity;

import org.springframework.security.oauth2.jwt.Jwt;

public interface IdentityResolver {
    AetherPrincipal resolve(Jwt jwt);
}
