package org.aether.provider.api;

import jakarta.validation.Valid;
import org.aether.provider.service.ProviderService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/providers")
public class ProviderController {

    private final ProviderService providerService;

    public ProviderController(ProviderService providerService) {
        this.providerService = providerService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProviderResponse create(
            @Valid @RequestBody ProviderRequest request) {

        return providerService.create(request);
    }

    @GetMapping
    public org.springframework.data.domain.Page<ProviderResponse> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return providerService.findAll(page, size);
    }

    @GetMapping("/{id}")
    public ProviderResponse findById(@PathVariable UUID id) {
        return providerService.findById(id);
    }

    @PutMapping("/{id}")
    public ProviderResponse update(@PathVariable UUID id, @Valid @RequestBody ProviderRequest request) {
        return providerService.update(id, request);
    }

    @PatchMapping("/{id}")
    public ProviderResponse patch(@PathVariable UUID id, @Valid @RequestBody ProviderPatchRequest request) {
        return providerService.patch(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        providerService.delete(id);
    }

    @PostMapping("/{id}/enable")
    public ProviderResponse enable(@PathVariable UUID id) {
        return providerService.enable(id);
    }

    @PostMapping("/{id}/disable")
    public ProviderResponse disable(@PathVariable UUID id) {
        return providerService.disable(id);
    }
}
