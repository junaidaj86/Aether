package org.aether.ai_models.repository;

import org.aether.ai_models.domain.Model;
import org.aether.ai_models.domain.ModelStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ModelRepository
        extends JpaRepository<Model, UUID> {

    List<Model> findByProviderIdAndStatusNot(UUID providerId, ModelStatus status);

    boolean existsByProviderIdAndProviderModelId(
            UUID providerId,
            String providerModelId
    );

    boolean existsByProviderIdAndProviderModelIdAndIdNot(
            UUID providerId, String providerModelId, UUID id);
}
