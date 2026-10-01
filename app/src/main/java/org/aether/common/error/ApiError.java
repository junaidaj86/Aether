package org.aether.common.error;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record ApiError(

        String code,

        String message,

        int status,

        String path,

        String correlationId,

        Instant timestamp,

        List<ApiFieldError> errors

) {
}