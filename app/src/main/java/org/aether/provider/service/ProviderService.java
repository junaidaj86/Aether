package org.aether.provider.service;

import org.aether.provider.api.ProviderPatchRequest;
import org.aether.provider.api.ProviderRequest;
import org.aether.provider.api.ProviderResponse;
import org.aether.provider.domain.Provider;
import org.aether.provider.domain.ProviderStatus;
import org.aether.provider.exception.ProviderAlreadyExistsException;
import org.aether.provider.exception.ProviderNotFoundException;
import org.aether.provider.repository.ProviderRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

@Service
public class ProviderService {
    private static final Logger log = LoggerFactory.getLogger(ProviderService.class);
    private final ProviderRepository providerRepository;

    public ProviderService(ProviderRepository providerRepository) {
        this.providerRepository = providerRepository;
    }

    @Transactional
    public ProviderResponse create(ProviderRequest request) {
        if (providerRepository.existsByNameAndEnvironment(request.name(), request.environment())) {
            throw new ProviderAlreadyExistsException(request.name(), request.environment());
        }
        Provider provider = providerRepository.save(new Provider(
                request.name(), request.type(), request.baseUrl(), request.environment()));
        log.info("event=provider_created providerId={} name={} environment={}",
                provider.getId(), provider.getName(), provider.getEnvironment());
        return toResponse(provider);
    }

    @Transactional(readOnly = true)
    public Page<ProviderResponse> findAll(int page, int size) {
        validatePage(page, size);
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        return providerRepository.findAllByStatusNot(ProviderStatus.DECOMMISSIONED, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public ProviderResponse findById(UUID id) {
        return toResponse(findVisible(id));
    }

    @Transactional
    public ProviderResponse update(UUID id, ProviderRequest request) {
        Provider provider = findVisible(id);
        ensureUniqueName(request.name(), request.environment(), id);
        provider.update(request.name(), request.type(), request.baseUrl(), request.environment());
        log.info("event=provider_updated providerId={} name={} environment={}",
                provider.getId(), provider.getName(), provider.getEnvironment());
        return toResponse(provider);
    }

    @Transactional
    public ProviderResponse patch(UUID id, ProviderPatchRequest request) {
        Provider provider = findVisible(id);
        if (request.name() == null && request.type() == null && request.baseUrl() == null
                && request.environment() == null) {
            throw new IllegalArgumentException("At least one field must be supplied for patch");
        }
        String name = request.name() == null ? provider.getName() : request.name();
        String environment = request.environment() == null ? provider.getEnvironment() : request.environment();
        ensureUniqueName(name, environment, id);
        provider.patch(request.name(), request.type(), request.baseUrl(), request.environment());
        log.info("event=provider_patched providerId={} name={} environment={}",
                provider.getId(), provider.getName(), provider.getEnvironment());
        return toResponse(provider);
    }

    @Transactional
    public void delete(UUID id) {
        Provider provider = findVisible(id);
        provider.decommission();
        log.info("event=provider_decommissioned providerId={} name={}", provider.getId(), provider.getName());
    }

    @Transactional
    public ProviderResponse enable(UUID id) {
        Provider provider = findVisible(id);
        provider.enable();
        log.info("event=provider_enabled providerId={} name={}", provider.getId(), provider.getName());
        return toResponse(provider);
    }

    @Transactional
    public ProviderResponse disable(UUID id) {
        Provider provider = findVisible(id);
        provider.disable();
        log.info("event=provider_disabled providerId={} name={}", provider.getId(), provider.getName());
        return toResponse(provider);
    }

    private Provider findVisible(UUID id) {
        Provider provider = providerRepository.findById(id)
                .orElseThrow(() -> new ProviderNotFoundException(id));
        if (provider.getStatus() == ProviderStatus.DECOMMISSIONED) {
            throw new ProviderNotFoundException(id);
        }
        return provider;
    }

    private void ensureUniqueName(String name, String environment, UUID id) {
        if (providerRepository.existsByNameAndEnvironmentAndIdNot(name, environment, id)) {
            throw new ProviderAlreadyExistsException(name, environment);
        }
    }

    private void validatePage(int page, int size) {
        if (page < 0 || size < 1 || size > 100) {
            throw new IllegalArgumentException("page must be non-negative and size must be between 1 and 100");
        }
    }

    private ProviderResponse toResponse(Provider provider) {
        return new ProviderResponse(provider.getId(), provider.getName(), provider.getType(),
                provider.getBaseUrl(), provider.getEnvironment(), provider.getStatus(),
                provider.getCreatedAt(), provider.getUpdatedAt());
    }
}
