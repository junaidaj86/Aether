package org.aether.governance;

import org.aether.provider.domain.Provider;
import org.aether.provider.domain.ProviderCredential;
import org.aether.provider.domain.ProviderType;
import org.aether.provider.repository.ProviderCredentialRepository;
import org.aether.provider.repository.ProviderRepository;
import org.aether.authentication.domain.CredentialStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@org.junit.jupiter.api.Tag("integration")
@EnabledIfEnvironmentVariable(named = "RUN_POSTGRES_INTEGRATION", matches = "true")
class PostgresRepositoryIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("aether")
                    .withUsername("aether")
                    .withPassword("aether");

    @DynamicPropertySource
    static void postgresProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.flyway.enabled", () -> true);
        registry.add("spring.flyway.locations", () -> "classpath:db/migration");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
    }

    @Autowired
    private ProviderRepository providerRepository;

    @Autowired
    private ProviderCredentialRepository credentialRepository;

    @Test
    void persistsProviderCredentialAgainstMigratedPostgresSchema() {
        Provider provider = providerRepository.saveAndFlush(
                new Provider("openai", ProviderType.OPENAI, "https://api.openai.com", "TESTING"));

        ProviderCredential credential = credentialRepository.saveAndFlush(
                new ProviderCredential(provider, "primary", "vault://aether/openai/primary",
                        CredentialStatus.ACTIVE, Instant.now(), null));

        assertThat(credentialRepository.findByProviderId(provider.getId()))
                .extracting(ProviderCredential::getName)
                .containsExactly("primary");
    }
}
