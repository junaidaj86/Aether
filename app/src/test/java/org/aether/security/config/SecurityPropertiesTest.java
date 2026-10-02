package org.aether.security.config;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.aether.security.identity.IdentityProvider;
import org.aether.security.jwt.TrustedIssuer;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityPropertiesTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void requiresAtLeastOneFullyConfiguredIssuerWhenEnabled() {
        SecurityProperties properties = new SecurityProperties();

        assertThat(validator.validate(properties))
                .extracting(error -> error.getPropertyPath().toString())
                .contains("uniqueIssuerNames");
    }

    @Test
    void rejectsDuplicateIssuerNames() {
        TrustedIssuer first = issuer("same-name", "https://one.example.test");
        TrustedIssuer second = issuer("same-name", "https://two.example.test");
        SecurityProperties properties = new SecurityProperties();
        properties.setIssuers(List.of(first, second));

        assertThat(validator.validate(properties))
                .extracting(error -> error.getPropertyPath().toString())
                .contains("uniqueIssuerNames");
    }

    @Test
    void allowsDisabledSecurityForLocalNonSecurityTests() {
        SecurityProperties properties = new SecurityProperties();
        properties.setEnabled(false);

        assertThat(validator.validate(properties)).isEmpty();
    }

    private TrustedIssuer issuer(String name, String uri) {
        TrustedIssuer issuer = new TrustedIssuer();
        issuer.setName(name);
        issuer.setProvider(IdentityProvider.GENERIC_OIDC);
        issuer.setIssuerUri(uri);
        issuer.setAudience("aether-api");
        issuer.setPrincipalClaim("sub");
        return issuer;
    }
}
