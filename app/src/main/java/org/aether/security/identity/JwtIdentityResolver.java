package org.aether.security.identity;

import org.aether.security.config.SecurityProperties;
import org.aether.security.jwt.TrustedIssuer;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Component
public class JwtIdentityResolver implements IdentityResolver {

    private final SecurityProperties properties;

    public JwtIdentityResolver(SecurityProperties properties) {
        this.properties = properties;
    }

    @Override
    public AetherPrincipal resolve(Jwt jwt) {
        TrustedIssuer issuer = properties.findByIssuer(jwt.getIssuer().toString());
        if (issuer == null) {
            throw invalidToken("Untrusted issuer");
        }

        String principalId = jwt.getClaimAsString(issuer.getPrincipalClaim());
        if (principalId == null || principalId.isBlank()) {
            throw invalidToken("Required principal claim is missing");
        }

        Set<String> roles = normalizeRoles(jwt);
        String displayName = firstNonBlank(
                jwt.getClaimAsString("preferred_username"),
                jwt.getClaimAsString("name"),
                principalId);

        return new AetherPrincipal(
                jwt.getIssuer().toString(),
                issuer.getProvider(),
                principalId,
                displayName,
                roles,
                safeAttributes(jwt));
    }

    private Set<String> normalizeRoles(Jwt jwt) {
        Set<String> roles = new HashSet<>();
        addCollectionClaim(roles, jwt.getClaim("roles"));
        addScopeClaim(roles, jwt.getClaimAsString("scope"));
        addScopeClaim(roles, jwt.getClaimAsString("scp"));

        Object realmAccess = jwt.getClaim("realm_access");
        if (realmAccess instanceof Map<?, ?> claims) {
            addCollectionClaim(roles, claims.get("roles"));
        }
        return Set.copyOf(roles);
    }

    private void addScopeClaim(Set<String> roles, String value) {
        if (value != null && !value.isBlank()) {
            for (String role : value.split("\\s+")) {
                if (!role.isBlank()) roles.add(role);
            }
        }
    }

    private void addCollectionClaim(Set<String> roles, Object value) {
        if (value instanceof Collection<?> values) {
            values.stream()
                    .filter(String.class::isInstance)
                    .map(String.class::cast)
                    .filter(role -> !role.isBlank())
                    .forEach(roles::add);
        }
    }

    private Map<String, Object> safeAttributes(Jwt jwt) {
        Map<String, Object> attributes = new LinkedHashMap<>();
        copyIfPresent(attributes, jwt, "sub");
        copyIfPresent(attributes, jwt, "azp");
        copyIfPresent(attributes, jwt, "appid");
        copyIfPresent(attributes, jwt, "tid");
        copyIfPresent(attributes, jwt, "preferred_username");
        copyIfPresent(attributes, jwt, "name");
        return Map.copyOf(attributes);
    }

    private void copyIfPresent(Map<String, Object> target, Jwt jwt, String claim) {
        Object value = jwt.getClaims().get(claim);
        if (value != null) target.put(claim, value);
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) return value;
        }
        return null;
    }

    private OAuth2AuthenticationException invalidToken(String message) {
        return new OAuth2AuthenticationException(
                new OAuth2Error(OAuth2ErrorCodes.INVALID_TOKEN), message);
    }
}
