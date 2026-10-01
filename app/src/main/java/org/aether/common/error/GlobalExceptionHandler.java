package org.aether.common.error;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AetherException.class)
    public ResponseEntity<ApiError> handleAetherException(
            AetherException exception,
            HttpServletRequest request
    ) {

        HttpStatus status = resolveStatus(
                exception.getErrorCode()
        );

        ApiError error = new ApiError(
                exception.getErrorCode().name(),
                exception.getMessage(),
                status.value(),
                request.getRequestURI(),
                null,
                Instant.now(),
                null
        );

        return ResponseEntity
                .status(status)
                .body(error);
    }


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

        ApiError error = new ApiError(
                ErrorCode.VALIDATION_ERROR.name(),
                "Request validation failed",
                HttpStatus.BAD_REQUEST.value(),
                request.getRequestURI(),
                null,
                Instant.now(),
                fieldErrors
        );

        return ResponseEntity
                .badRequest()
                .body(error);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleConflict(HttpServletRequest request) {
        return response(ErrorCode.CONFLICT, "The request conflicts with an existing resource",
                HttpStatus.CONFLICT, request);
    }

    @ExceptionHandler({IllegalArgumentException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ApiError> handleBadRequest(Exception exception, HttpServletRequest request) {
        return response(ErrorCode.INVALID_REQUEST, exception.getMessage(), HttpStatus.BAD_REQUEST, request);
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpectedException(
            Exception exception,
            HttpServletRequest request
    ) {

        ApiError error = new ApiError(
                ErrorCode.INTERNAL_ERROR.name(),
                "An unexpected error occurred",
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                request.getRequestURI(),
                null,
                Instant.now(),
                null
        );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(error);
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

            case INTERNAL_ERROR ->
                    HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }

    private ResponseEntity<ApiError> response(
            ErrorCode code, String message, HttpStatus status, HttpServletRequest request) {
        return ResponseEntity.status(status).body(new ApiError(
                code.name(), message, status.value(), request.getRequestURI(), null, Instant.now(), null));
    }
}
