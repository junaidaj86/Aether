package org.aether.ai_models.exception;

import org.aether.common.error.AetherException;
import org.aether.common.error.ErrorCode;

import java.util.UUID;

public class ModelNotFoundException extends AetherException {

    public ModelNotFoundException(UUID modelId) {
        super(
                ErrorCode.MODEL_NOT_FOUND,
                "Model not found: " + modelId
        );
    }
}