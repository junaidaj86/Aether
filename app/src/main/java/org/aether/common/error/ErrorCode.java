package org.aether.common.error;

public enum ErrorCode {

    // General
    INVALID_REQUEST,
    VALIDATION_ERROR,
    RESOURCE_NOT_FOUND,
    CONFLICT,
    INTERNAL_ERROR,

    // Agent
    AGENT_NOT_FOUND,
    AGENT_ALREADY_EXISTS,

    // Provider
    PROVIDER_NOT_FOUND,
    PROVIDER_ALREADY_EXISTS,
    PROVIDER_DISABLED,

    // Model
    MODEL_NOT_FOUND,
    MODEL_ALREADY_EXISTS,
    MODEL_DISABLED
}