package org.aether.security.identity;

import org.aether.security.config.SecurityProperties;
import org.aether.security.jwt.TrustedIssuer;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class JwtIdentityResolverTest {

    @Test
    void normalizesRolesFromCommonScopeRolesAndKeycloakClaims() {
        SecurityProperties properties = properties();
        JwtIdentityResolver resolver = new JwtIdentityResolver(properties);
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "RS256")
                .issuer("https://issuer.example.test")
                .subject("subject-123")
                .claim("preferred_username", "agent-client")
                .claim("scope", "aether.inference aether.registry.read")
                .claim("roles", List.of("jira-reader"))
                .claim("realm_access", Map.of("roles", List.of("confluence-reader")))
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(300))
                .build();

        AetherPrincipal principal = resolver.resolve(jwt);

        assertThat(principal.identityProvider()).isEqualTo(IdentityProvider.KEYCLOAK);
        assertThat(principal.externalPrincipalId()).isEqualTo("subject-123");
        assertThat(principal.displayName()).isEqualTo("agent-client");
        assertThat(principal.roles()).containsExactlyInAnyOrder(
                "aether.inference", "aether.registry.read", "jira-reader", "confluence-reader");
    }

    @Test
    void usesConfiguredPrincipalClaimForAnotherProvider() {
        SecurityProperties properties = properties();
        properties.getIssuers().get(0).setProvider(IdentityProvider.ENTRA_ID);
        properties.getIssuers().get(0).setPrincipalClaim("appid");
        JwtIdentityResolver resolver = new JwtIdentityResolver(properties);
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "RS256")
                .issuer("https://issuer.example.test")
                .subject("user-subject")
                .claim("appid", "service-principal-123")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(300))
                .build();

        AetherPrincipal principal = resolver.resolve(jwt);

        assertThat(principal.identityProvider()).isEqualTo(IdentityProvider.ENTRA_ID);
        assertThat(principal.externalPrincipalId()).isEqualTo("service-principal-123");
    }

    private SecurityProperties properties() {
        TrustedIssuer issuer = new TrustedIssuer();
        issuer.setName("local");
        issuer.setProvider(IdentityProvider.KEYCLOAK);
        issuer.setIssuerUri("https://issuer.example.test");
        issuer.setAudience("aether-api");
        issuer.setPrincipalClaim("sub");

        SecurityProperties properties = new SecurityProperties();
        properties.setIssuers(List.of(issuer));
        return properties;
    }
}
