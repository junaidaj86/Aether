package org.aether.provider.exception;

import org.aether.common.error.AetherException;
import org.aether.common.error.ErrorCode;

import java.util.UUID;

public class ProviderDisabledException
        extends AetherException {

    public ProviderDisabledException(UUID providerId) {
        super(
                ErrorCode.PROVIDER_DISABLED,
                "Provider is disabled: " + providerId
        );
    }
}