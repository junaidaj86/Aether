package org.aether.ai_models.service;

import org.aether.ai_models.api.ModelPatchRequest;
import org.aether.ai_models.domain.Model;
import org.aether.ai_models.domain.ModelCapability;
import org.aether.ai_models.domain.ModelStatus;
import org.aether.ai_models.repository.ModelRepository;
import org.aether.provider.domain.Provider;
import org.aether.provider.domain.ProviderType;
import org.aether.provider.repository.ProviderRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ModelServiceTest {
    private final ModelRepository modelRepository = mock(ModelRepository.class);
    private final ProviderRepository providerRepository = mock(ProviderRepository.class);
    private final ModelService service = new ModelService(modelRepository, providerRepository);

    @Test
    void patchesModelWithoutClearingOmittedFields() {
        Provider provider = new Provider("openai", ProviderType.OPENAI, "https://api", "dev");
        Model model = new Model(provider, "fast", "gpt-fast", 1000, 500, Set.of(ModelCapability.TEXT));
        UUID id = model.getId();
        when(modelRepository.findById(id)).thenReturn(Optional.of(model));
        when(modelRepository.existsByProviderIdAndProviderModelIdAndIdNot(
                provider.getId(), "gpt-fast", id)).thenReturn(false);

        var result = service.patch(id, new ModelPatchRequest("faster", null, null, null, null));

        assertEquals("faster", result.name());
        assertEquals("gpt-fast", result.providerModelId());
        assertEquals(Set.of(ModelCapability.TEXT), result.capabilities());
    }

    @Test
    void deleteSoftDecommissionsModel() {
        Provider provider = new Provider("openai", ProviderType.OPENAI, "https://api", "dev");
        Model model = new Model(provider, "fast", "gpt-fast", 1000, 500, Set.of());
        when(modelRepository.findById(model.getId())).thenReturn(Optional.of(model));

        service.delete(model.getId());

        assertEquals(ModelStatus.DECOMMISSIONED, model.getStatus());
    }
}
