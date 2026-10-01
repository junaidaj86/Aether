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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;

@Service
public class ModelService {
    private static final Logger log = LoggerFactory.getLogger(ModelService.class);
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
        Model model = modelRepository.save(new Model(provider, request.name(), request.providerModelId(),
                request.contextWindow(), request.maxOutputTokens(), request.capabilities()));
        log.info("event=model_created modelId={} providerId={} name={}",
                model.getId(), provider.getId(), model.getName());
        return toResponse(model);
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
        log.info("event=model_updated modelId={} providerId={} name={}",
                model.getId(), model.getProvider().getId(), model.getName());
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
        log.info("event=model_patched modelId={} providerId={} name={}",
                model.getId(), model.getProvider().getId(), model.getName());
        return toResponse(model);
    }

    @Transactional
    public void delete(UUID modelId) {
        Model model = findVisible(modelId);
        model.decommission();
        log.info("event=model_decommissioned modelId={} providerId={} name={}",
                model.getId(), model.getProvider().getId(), model.getName());
    }

    @Transactional
    public ModelResponse enable(UUID modelId) {
        Model model = findVisible(modelId);
        model.enable();
        log.info("event=model_enabled modelId={} name={}", model.getId(), model.getName());
        return toResponse(model);
    }

    @Transactional
    public ModelResponse disable(UUID modelId) {
        Model model = findVisible(modelId);
        model.disable();
        log.info("event=model_disabled modelId={} name={}", model.getId(), model.getName());
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
