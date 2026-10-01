package org.aether.provider.exception;

import org.aether.common.error.AetherException;
import org.aether.common.error.ErrorCode;

public class ProviderAlreadyExistsException
        extends AetherException {

    public ProviderAlreadyExistsException(
            String name,
            String environment
    ) {
        super(
                ErrorCode.PROVIDER_ALREADY_EXISTS,
                "Provider already exists: "
                        + name
                        + " in environment "
                        + environment
        );
    }
}