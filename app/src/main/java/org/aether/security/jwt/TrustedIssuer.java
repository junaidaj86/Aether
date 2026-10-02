package org.aether.security.jwt;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.aether.security.identity.IdentityProvider;

public class TrustedIssuer {

    @NotBlank
    private String name;

    @NotNull
    private IdentityProvider provider;

    @NotBlank
    private String issuerUri;

    @NotBlank
    private String audience;

    @NotBlank
    private String principalClaim;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public IdentityProvider getProvider() { return provider; }
    public void setProvider(IdentityProvider provider) { this.provider = provider; }
    public String getIssuerUri() { return issuerUri; }
    public void setIssuerUri(String issuerUri) { this.issuerUri = issuerUri; }
    public String getAudience() { return audience; }
    public void setAudience(String audience) { this.audience = audience; }
    public String getPrincipalClaim() { return principalClaim; }
    public void setPrincipalClaim(String principalClaim) { this.principalClaim = principalClaim; }
}
