package org.aether.provider.exception;

import org.aether.common.error.AetherException;
import org.aether.common.error.ErrorCode;

import java.util.UUID;

public class ProviderNotFoundException extends AetherException {

    public ProviderNotFoundException(UUID providerId) {
        super(
                ErrorCode.PROVIDER_NOT_FOUND,
                "Provider not found: " + providerId
        );
    }
}