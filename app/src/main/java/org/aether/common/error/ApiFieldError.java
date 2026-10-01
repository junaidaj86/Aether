package org.aether.common.error;

public record ApiFieldError(
        String field,
        String message
) {
}