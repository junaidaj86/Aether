package org.aether.provider.repository;

import org.aether.provider.domain.ProviderCredential;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ProviderCredentialRepository extends JpaRepository<ProviderCredential, UUID> {
    List<ProviderCredential> findByProviderId(UUID providerId);
}
