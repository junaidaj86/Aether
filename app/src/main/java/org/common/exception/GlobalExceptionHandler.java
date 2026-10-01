package org.common.exception;

import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    public record ApiError(String code, String message) {}

    public record ValidationErrorResponse(
            String code,
            String message,
            List<ValidationFieldError> errors) {}

    public record ValidationFieldError(String field, String message) {}

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidation(
            MethodArgumentNotValidException exception) {

        var errors = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> new ValidationFieldError(error.getField(), error.getDefaultMessage()))
                .toList();

        return ResponseEntity
                .badRequest()
                .body(new ValidationErrorResponse(
                        "VALIDATION_ERROR",
                        "Request validation failed",
                        errors));
    }

    @ExceptionHandler(AgentNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(AgentNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiError("AGENT_NOT_FOUND", exception.getMessage()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleConflict() {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiError("AGENT_CONFLICT", "An agent with the same unique identity already exists"));
    }

    @ExceptionHandler({IllegalArgumentException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ApiError> handleBadRequest(Exception exception) {
        return ResponseEntity.badRequest()
                .body(new ApiError("BAD_REQUEST", exception.getMessage()));
    }
}
