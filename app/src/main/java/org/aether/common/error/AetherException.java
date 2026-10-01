package org.aether.common.error;

public abstract class AetherException extends RuntimeException {

    private final ErrorCode errorCode;

    protected AetherException(
            ErrorCode errorCode,
            String message
    ) {
        super(message);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}