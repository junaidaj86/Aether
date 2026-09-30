package org.common.exception;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

        public record ValidationErrorResponse(
                        String code,
                        String message,
                        List<FieldError> errors) {
        }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidation(
            MethodArgumentNotValidException exception) {

        var errors = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> new FieldError(
                        error.getObjectName(),
                        error.getField(),
                        error.getDefaultMessage()))
                .toList();

        return ResponseEntity
                .badRequest()
                .body(new ValidationErrorResponse(
                        "VALIDATION_ERROR",
                        "Request validation failed",
                        errors));
    }
}