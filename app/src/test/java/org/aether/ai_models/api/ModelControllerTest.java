package org.aether.ai_models.api;

import org.aether.ai_models.domain.ModelCapability;
import org.aether.ai_models.domain.ModelStatus;
import org.aether.ai_models.service.ModelService;
import org.aether.common.error.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ModelController.class)
@Import(GlobalExceptionHandler.class)
class ModelControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ModelService modelService;

    private final UUID id = UUID.randomUUID();

    @Test
    void deleteReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/v1/models/{modelId}", id))
                .andExpect(status().isNoContent());

        verify(modelService).delete(id);
    }

    @Test
    void enableReturnsOk() throws Exception {
        when(modelService.enable(id)).thenReturn(response());

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .post("/api/v1/models/{modelId}/enable", id))
                .andExpect(status().isOk());
    }

    private ModelResponse response() {
        return new ModelResponse(id, UUID.randomUUID(), "openai", "fast", "gpt-fast",
                ModelStatus.ACTIVE, 1000, 500, Set.of(ModelCapability.TEXT),
                Instant.parse("2026-01-01T00:00:00Z"), Instant.parse("2026-01-01T00:00:00Z"));
    }
}
