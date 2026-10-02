package org.aether.security.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import org.aether.security.jwt.TrustedIssuer;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Validated
@ConfigurationProperties(prefix = "aether.security")
public class SecurityProperties {

    private boolean enabled = true;

    @Valid
    private List<TrustedIssuer> issuers = List.of();

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public List<TrustedIssuer> getIssuers() { return issuers; }
    public void setIssuers(List<TrustedIssuer> issuers) { this.issuers = issuers; }

    @AssertTrue(message = "trusted issuer names must be unique")
    public boolean hasUniqueIssuerNames() {
        return !enabled || (issuers != null
                && !issuers.isEmpty()
                && issuers.stream().map(TrustedIssuer::getName).distinct().count() == issuers.size());
    }

    public TrustedIssuer findByIssuer(String issuer) {
        return issuers.stream()
                .filter(candidate -> candidate.getIssuerUri().equals(issuer))
                .findFirst()
                .orElse(null);
    }
}
