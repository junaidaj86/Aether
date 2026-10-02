package org.aether.security.identity;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.authentication.AbstractAuthenticationToken;

import java.util.Collection;

public final class AetherAuthenticationToken extends AbstractAuthenticationToken {

    private final AetherPrincipal principal;
    private final Jwt token;

    public AetherAuthenticationToken(
            AetherPrincipal principal,
            Jwt token,
            Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.principal = principal;
        this.token = token;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return "[PROTECTED]";
    }

    @Override
    public AetherPrincipal getPrincipal() {
        return principal;
    }

    Jwt getToken() {
        return token;
    }
}
