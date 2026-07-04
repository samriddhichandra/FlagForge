package com.flagforge.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    public record ErrorResponse(String error, String message, Instant timestamp) {}

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("error", "VALIDATION_ERROR");
        body.put("timestamp", Instant.now());
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(fe -> fieldErrors.put(fe.getField(), fe.getDefaultMessage()));
        body.put("fields", fieldErrors);
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(ApiExceptions.FlagNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ApiExceptions.FlagNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse("FLAG_NOT_FOUND", ex.getMessage(), Instant.now()));
    }

    @ExceptionHandler(ApiExceptions.DuplicateFlagKeyException.class)
    public ResponseEntity<ErrorResponse> handleDuplicate(ApiExceptions.DuplicateFlagKeyException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse("DUPLICATE_KEY", ex.getMessage(), Instant.now()));
    }

    @ExceptionHandler(ApiExceptions.InsufficientRoleException.class)
    public ResponseEntity<ErrorResponse> handleForbidden(ApiExceptions.InsufficientRoleException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ErrorResponse("FORBIDDEN", ex.getMessage(), Instant.now()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        // In production this also logs a correlation ID for traceability — omitted here for brevity
        return ResponseEntity.internalServerError()
                .body(new ErrorResponse("INTERNAL_ERROR", "An unexpected error occurred.", Instant.now()));
    }
}
