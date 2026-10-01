package org.aether.ai_models.exception;

import org.aether.common.error.AetherException;
import org.aether.common.error.ErrorCode;

public class ModelAlreadyExistsException
        extends AetherException {

    public ModelAlreadyExistsException(String providerModelId) {
        super(
                ErrorCode.MODEL_ALREADY_EXISTS,
                "Model already exists: " + providerModelId
        );
    }
}