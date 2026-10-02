package org.aether.common.error;

import jakarta.servlet.http.HttpServletRequest;

import org.aether.common.web.CorrelationIdFilter;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /*
     * Aether domain/business exceptions.
     */
    @ExceptionHandler(AetherException.class)
    public ResponseEntity<ApiError> handleAetherException(
            AetherException exception,
            HttpServletRequest request
    ) {

        HttpStatus status = resolveStatus(exception.getErrorCode());

        log.warn("event=request_rejected code={} status={} path={}",
                exception.getErrorCode(), status.value(), request.getRequestURI());

        return response(
                exception.getErrorCode(),
                exception.getMessage(),
                status,
                request
        );
    }


    /*
     * Bean validation errors:
     *
     * @NotBlank
     * @NotNull
     * @Positive
     * @Size
     * etc.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidationException(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {

        List<ApiFieldError> fieldErrors =
                exception.getBindingResult()
                        .getFieldErrors()
                        .stream()
                        .map(error ->
                                new ApiFieldError(
                                        error.getField(),
                                        error.getDefaultMessage()
                                )
                        )
                        .toList();

        log.warn("event=request_validation_failed path={} fieldCount={}",
                request.getRequestURI(), fieldErrors.size());

        ApiError error = new ApiError(
                ErrorCode.VALIDATION_ERROR.name(),
                "Request validation failed",
                HttpStatus.BAD_REQUEST.value(),
                request.getRequestURI(),
                getCorrelationId(request),
                Instant.now(),
                fieldErrors
        );

        return ResponseEntity
                .badRequest()
                .body(error);
    }


    /*
     * Invalid path-variable types.
     *
     * Example:
     *
     * GET /api/v1/models/test-model
     *
     * when modelId must be UUID.
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleTypeMismatch(
            MethodArgumentTypeMismatchException exception,
            HttpServletRequest request
    ) {

        return response(
                ErrorCode.INVALID_REQUEST,
                "Invalid value for parameter: " + exception.getName(),
                HttpStatus.BAD_REQUEST,
                request
        );
    }


    /*
     * Malformed JSON / invalid enum values.
     *
     * Example:
     *
     * "capabilities": ["SOMETHING_INVALID"]
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleMalformedRequest(
            HttpMessageNotReadableException exception,
            HttpServletRequest request
    ) {

        return response(
                ErrorCode.INVALID_REQUEST,
                "Request body is invalid or malformed",
                HttpStatus.BAD_REQUEST,
                request
        );
    }


    /*
     * Domain validation that throws IllegalArgumentException.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgument(
            IllegalArgumentException exception,
            HttpServletRequest request
    ) {

        return response(
                ErrorCode.INVALID_REQUEST,
                exception.getMessage(),
                HttpStatus.BAD_REQUEST,
                request
        );
    }


    /*
     * Database constraint violations.
     *
     * This should be a safety net.
     * Known conflicts should normally be detected by the service
     * and converted into a specific AetherException.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleConflict(
            DataIntegrityViolationException exception,
            HttpServletRequest request
    ) {

        return response(
                ErrorCode.CONFLICT,
                "The request conflicts with an existing resource",
                HttpStatus.CONFLICT,
                request
        );
    }


    /*
     * Last-resort exception handler.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpectedException(
            Exception exception,
            HttpServletRequest request
    ) {

        log.error("event=unexpected_request_failure path={}", request.getRequestURI(), exception);

        return response(
                ErrorCode.INTERNAL_ERROR,
                "An unexpected error occurred",
                HttpStatus.INTERNAL_SERVER_ERROR,
                request
        );
    }


    private ResponseEntity<ApiError> response(
            ErrorCode code,
            String message,
            HttpStatus status,
            HttpServletRequest request
    ) {

        ApiError error = new ApiError(
                code.name(),
                message,
                status.value(),
                request.getRequestURI(),
                getCorrelationId(request),
                Instant.now(),
                null
        );

        return ResponseEntity
                .status(status)
                .body(error);
    }


    private String getCorrelationId(
            HttpServletRequest request
    ) {

        Object correlationId =
                request.getAttribute(
                        CorrelationIdFilter.MDC_KEY
                );

        return correlationId != null
                ? correlationId.toString()
                : null;
    }


    private HttpStatus resolveStatus(ErrorCode errorCode) {

        return switch (errorCode) {

            case AGENT_NOT_FOUND,
                 PROVIDER_NOT_FOUND,
                 MODEL_NOT_FOUND,
                 RESOURCE_NOT_FOUND ->
                    HttpStatus.NOT_FOUND;

            case AGENT_ALREADY_EXISTS,
                 PROVIDER_ALREADY_EXISTS,
                 MODEL_ALREADY_EXISTS,
                 PROVIDER_DISABLED,
                 MODEL_DISABLED,
                 CONFLICT ->
                    HttpStatus.CONFLICT;

            case INVALID_REQUEST,
                 VALIDATION_ERROR ->
                    HttpStatus.BAD_REQUEST;

            case AUTHENTICATION_REQUIRED,
                 INVALID_TOKEN,
                 UNTRUSTED_ISSUER,
                 INVALID_AUDIENCE ->
                    HttpStatus.UNAUTHORIZED;

            case AGENT_NOT_REGISTERED,
                 ACCESS_DENIED ->
                    HttpStatus.FORBIDDEN;

            case INTERNAL_ERROR ->
                    HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }
}
