package org.aether.security.jwt;

import org.aether.security.identity.AetherAuthenticationToken;
import org.aether.security.identity.AetherPrincipal;
import org.aether.security.identity.IdentityResolver;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Set;

public class AetherJwtAuthenticationConverter
        implements Converter<Jwt, AbstractAuthenticationToken> {

    private final IdentityResolver identityResolver;

    public AetherJwtAuthenticationConverter(IdentityResolver identityResolver) {
        this.identityResolver = identityResolver;
    }

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        AetherPrincipal principal = identityResolver.resolve(jwt);
        Set<SimpleGrantedAuthority> authorities = principal.roles().stream()
                .map(role -> new SimpleGrantedAuthority(normalizeAuthority(role)))
                .collect(java.util.stream.Collectors.toUnmodifiableSet());

        return new AetherAuthenticationToken(principal, jwt, authorities);
    }

    private String normalizeAuthority(String role) {
        if (role.startsWith("SCOPE_") || role.startsWith("ROLE_")) {
            return role;
        }
        return role.startsWith("aether.")
                ? "ROLE_" + role
                : "ROLE_" + role;
    }
}
