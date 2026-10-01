package org.aether.provider.service;

import org.aether.provider.api.ProviderPatchRequest;
import org.aether.provider.api.ProviderRequest;
import org.aether.provider.domain.Provider;
import org.aether.provider.domain.ProviderStatus;
import org.aether.provider.domain.ProviderType;
import org.aether.provider.repository.ProviderRepository;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ProviderServiceTest {
    private final ProviderRepository repository = mock(ProviderRepository.class);
    private final ProviderService service = new ProviderService(repository);
    private final UUID id = UUID.randomUUID();

    @Test
    void updatesProviderAndPreservesIdentity() {
        Provider provider = new Provider("openai", ProviderType.OPENAI, "https://old", "dev");
        when(repository.findById(id)).thenReturn(Optional.of(provider));
        when(repository.existsByNameAndEnvironmentAndIdNot("new-name", "prod", id)).thenReturn(false);

        var result = service.update(id, new ProviderRequest("new-name", ProviderType.OPENAI,
                "https://new", "prod"));

        assertEquals("new-name", result.name());
        assertEquals("prod", result.environment());
        assertEquals(ProviderStatus.ACTIVE, result.status());
    }

    @Test
    void deleteSoftDecommissionsProvider() {
        Provider provider = new Provider("openai", ProviderType.OPENAI, "https://old", "dev");
        when(repository.findById(id)).thenReturn(Optional.of(provider));

        service.delete(id);

        assertEquals(ProviderStatus.DECOMMISSIONED, provider.getStatus());
    }

    @Test
    void listsOnlyVisibleProvidersWithPagination() {
        when(repository.findAllByStatusNot(org.mockito.ArgumentMatchers.eq(ProviderStatus.DECOMMISSIONED),
                org.mockito.ArgumentMatchers.any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 20), 0));

        assertEquals(0, service.findAll(0, 20).getTotalElements());
    }
}
