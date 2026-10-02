package org.aether.provider.api;

import org.aether.provider.domain.ProviderStatus;
import org.aether.provider.domain.ProviderType;
import org.aether.provider.service.ProviderService;
import org.aether.common.error.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProviderController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class ProviderControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProviderService providerService;

    private final UUID id = UUID.randomUUID();

    @Test
    void patchDelegatesToProviderService() throws Exception {
        when(providerService.patch(eq(id), org.mockito.ArgumentMatchers.any(ProviderPatchRequest.class)))
                .thenReturn(response());

        mockMvc.perform(patch("/api/v1/providers/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"baseUrl\":\"https://new.example\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void deleteReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/v1/providers/{id}", id))
                .andExpect(status().isNoContent());

        verify(providerService).delete(id);
    }

    private ProviderResponse response() {
        return new ProviderResponse(id, "openai", ProviderType.OPENAI, "https://api.example", "dev",
                ProviderStatus.ACTIVE, Instant.parse("2026-01-01T00:00:00Z"),
                Instant.parse("2026-01-01T00:00:00Z"));
    }
}
