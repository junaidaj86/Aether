package org.aether.ai_models.api;

import jakarta.validation.Valid;
import org.aether.ai_models.service.ModelService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class ModelController {

    private final ModelService modelService;

    public ModelController(ModelService modelService) {
        this.modelService = modelService;
    }

    @PostMapping("/providers/{providerId}/models")
    @ResponseStatus(HttpStatus.CREATED)
    public ModelResponse create(
            @PathVariable UUID providerId,
            @Valid @RequestBody ModelRequest request) {

        return modelService.create(providerId, request);
    }

    @GetMapping("/providers/{providerId}/models")
    public List<ModelResponse> findByProvider(
            @PathVariable UUID providerId) {

        return modelService.findByProvider(providerId);
    }

    @GetMapping("/models/{modelId}")
    public ModelResponse findById(
            @PathVariable UUID modelId) {

        return modelService.findById(modelId);
    }

    @PutMapping("/models/{modelId}")
    public ModelResponse update(@PathVariable UUID modelId, @Valid @RequestBody ModelRequest request) {
        return modelService.update(modelId, request);
    }

    @PatchMapping("/models/{modelId}")
    public ModelResponse patch(@PathVariable UUID modelId, @Valid @RequestBody ModelPatchRequest request) {
        return modelService.patch(modelId, request);
    }

    @DeleteMapping("/models/{modelId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID modelId) {
        modelService.delete(modelId);
    }

    @PostMapping("/models/{modelId}/enable")
    public ModelResponse enable(@PathVariable UUID modelId) {
        return modelService.enable(modelId);
    }

    @PostMapping("/models/{modelId}/disable")
    public ModelResponse disable(@PathVariable UUID modelId) {
        return modelService.disable(modelId);
    }
}
