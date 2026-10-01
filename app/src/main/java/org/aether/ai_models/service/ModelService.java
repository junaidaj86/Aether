package org.aether.ai_models.service;

import org.aether.ai_models.api.ModelPatchRequest;
import org.aether.ai_models.api.ModelRequest;
import org.aether.ai_models.api.ModelResponse;
import org.aether.ai_models.domain.Model;
import org.aether.ai_models.domain.ModelStatus;
import org.aether.ai_models.exception.ModelAlreadyExistsException;
import org.aether.ai_models.exception.ModelNotFoundException;
import org.aether.ai_models.repository.ModelRepository;
import org.aether.provider.domain.Provider;
import org.aether.provider.domain.ProviderStatus;
import org.aether.provider.exception.ProviderDisabledException;
import org.aether.provider.exception.ProviderNotFoundException;
import org.aether.provider.repository.ProviderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ModelService {
    private final ModelRepository modelRepository;
    private final ProviderRepository providerRepository;

    public ModelService(ModelRepository modelRepository, ProviderRepository providerRepository) {
        this.modelRepository = modelRepository;
        this.providerRepository = providerRepository;
    }

    @Transactional
    public ModelResponse create(UUID providerId, ModelRequest request) {
        Provider provider = providerRepository.findById(providerId)
                .orElseThrow(() -> new ProviderNotFoundException(providerId));
        if (provider.getStatus() != ProviderStatus.ACTIVE) {
            throw new ProviderDisabledException(providerId);
        }
        ensureUnique(providerId, request.providerModelId(), null);
        return toResponse(modelRepository.save(new Model(provider, request.name(), request.providerModelId(),
                request.contextWindow(), request.maxOutputTokens(), request.capabilities())));
    }

    @Transactional(readOnly = true)
    public ModelResponse findById(UUID modelId) {
        return toResponse(findVisible(modelId));
    }

    @Transactional(readOnly = true)
    public List<ModelResponse> findByProvider(UUID providerId) {
        providerRepository.findById(providerId)
                .filter(provider -> provider.getStatus() != ProviderStatus.DECOMMISSIONED)
                .orElseThrow(() -> new ProviderNotFoundException(providerId));
        return modelRepository.findByProviderIdAndStatusNot(providerId, ModelStatus.DECOMMISSIONED)
                .stream().map(this::toResponse).toList();
    }

    @Transactional
    public ModelResponse update(UUID modelId, ModelRequest request) {
        Model model = findVisible(modelId);
        ensureUnique(model.getProvider().getId(), request.providerModelId(), modelId);
        model.update(request.name(), request.providerModelId(), request.contextWindow(),
                request.maxOutputTokens(), request.capabilities());
        return toResponse(model);
    }

    @Transactional
    public ModelResponse patch(UUID modelId, ModelPatchRequest request) {
        Model model = findVisible(modelId);
        if (request.name() == null && request.providerModelId() == null && request.contextWindow() == null
                && request.maxOutputTokens() == null && request.capabilities() == null) {
            throw new IllegalArgumentException("At least one field must be supplied for patch");
        }
        String providerModelId = request.providerModelId() == null
                ? model.getProviderModelId() : request.providerModelId();
        ensureUnique(model.getProvider().getId(), providerModelId, modelId);
        model.patch(request.name(), request.providerModelId(), request.contextWindow(),
                request.maxOutputTokens(), request.capabilities());
        return toResponse(model);
    }

    @Transactional
    public void delete(UUID modelId) {
        findVisible(modelId).decommission();
    }

    @Transactional
    public ModelResponse enable(UUID modelId) {
        Model model = findVisible(modelId);
        model.enable();
        return toResponse(model);
    }

    @Transactional
    public ModelResponse disable(UUID modelId) {
        Model model = findVisible(modelId);
        model.disable();
        return toResponse(model);
    }

    private Model findVisible(UUID modelId) {
        Model model = modelRepository.findById(modelId)
                .orElseThrow(() -> new ModelNotFoundException(modelId));
        if (model.getStatus() == ModelStatus.DECOMMISSIONED) {
            throw new ModelNotFoundException(modelId);
        }
        return model;
    }

    private void ensureUnique(UUID providerId, String providerModelId, UUID currentId) {
        boolean exists = currentId == null
                ? modelRepository.existsByProviderIdAndProviderModelId(providerId, providerModelId)
                : modelRepository.existsByProviderIdAndProviderModelIdAndIdNot(providerId, providerModelId, currentId);
        if (exists) {
            throw new ModelAlreadyExistsException(providerModelId);
        }
    }

    private ModelResponse toResponse(Model model) {
        return new ModelResponse(model.getId(), model.getProvider().getId(), model.getProvider().getName(),
                model.getName(), model.getProviderModelId(), model.getStatus(), model.getContextWindow(),
                model.getMaxOutputTokens(), model.getCapabilities(), model.getCreatedAt(), model.getUpdatedAt());
    }
}
