package org.aether.security.config;

import jakarta.servlet.http.HttpServletRequest;
import org.aether.security.identity.IdentityResolver;
import org.aether.security.jwt.AetherJwtAuthenticationConverter;
import org.aether.security.jwt.TrustedIssuer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationProvider;
import org.springframework.security.oauth2.server.resource.authentication.JwtIssuerAuthenticationManagerResolver;
import org.springframework.security.authentication.AuthenticationManagerResolver;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
@EnableMethodSecurity
@EnableConfigurationProperties(SecurityProperties.class)
public class SecurityConfig {

    @Bean
    public AuthenticationManagerResolver<HttpServletRequest> authenticationManagerResolver(
            SecurityProperties properties,
            IdentityResolver identityResolver) {

        Map<String, AuthenticationManager> managers = new LinkedHashMap<>();
        if (!properties.isEnabled()) {
            return request -> null;
        }
        AetherJwtAuthenticationConverter converter =
                new AetherJwtAuthenticationConverter(identityResolver);

        for (TrustedIssuer issuer : properties.getIssuers()) {
            JwtDecoder decoder = decoderFor(issuer);
            JwtAuthenticationProvider provider = new JwtAuthenticationProvider(decoder);
            provider.setJwtAuthenticationConverter(converter);
            managers.put(issuer.getIssuerUri(), new ProviderManager(provider));
        }

        return new JwtIssuerAuthenticationManagerResolver(issuer -> {
            AuthenticationManager manager = managers.get(issuer);
            if (manager == null) {
                throw new OAuth2AuthenticationException(
                        new OAuth2Error("untrusted_issuer"));
            }
            return manager;
        });
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            SecurityProperties properties,
            AuthenticationManagerResolver<HttpServletRequest> resolver,
            AuthenticationEntryPoint authenticationEntryPoint,
            AccessDeniedHandler accessDeniedHandler) throws Exception {

        http.csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(
                        SessionCreationPolicy.STATELESS))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler));

        if (!properties.isEnabled()) {
            http.authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll());
            return http.build();
        }

        http.authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(
                                "/actuator/health",
                                "/actuator/health/liveness",
                                "/actuator/health/readiness")
                        .permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/**")
                        .hasAnyAuthority("ROLE_aether.registry.read", "ROLE_aether.admin")
                        .requestMatchers("/api/**")
                        .hasAnyAuthority("ROLE_aether.registry.write", "ROLE_aether.admin")
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2
                        .authenticationManagerResolver(resolver)
                        .authenticationEntryPoint(authenticationEntryPoint));

        return http.build();
    }

    private JwtDecoder decoderFor(TrustedIssuer issuer) {
        NimbusJwtDecoder decoder = (NimbusJwtDecoder) JwtDecoders
                .fromIssuerLocation(issuer.getIssuerUri());

        OAuth2TokenValidator<Jwt> issuerValidator =
                JwtValidators.createDefaultWithIssuer(issuer.getIssuerUri());
        OAuth2TokenValidator<Jwt> audienceValidator = jwt ->
                jwt.getAudience().contains(issuer.getAudience())
                        ? org.springframework.security.oauth2.core.OAuth2TokenValidatorResult.success()
                        : org.springframework.security.oauth2.core.OAuth2TokenValidatorResult.failure(
                                new OAuth2Error("invalid_audience"));

        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                issuerValidator, audienceValidator));
        return decoder;
    }
}
