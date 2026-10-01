package org.aether.provider.repository;

import org.aether.provider.domain.Provider;
import org.aether.provider.domain.ProviderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ProviderRepository
        extends JpaRepository<Provider, UUID> {

    boolean existsByNameAndEnvironment(
            String name,
            String environment
    );

    boolean existsByNameAndEnvironmentAndIdNot(String name, String environment, UUID id);

    Page<Provider> findAllByStatusNot(ProviderStatus status, Pageable pageable);
}
