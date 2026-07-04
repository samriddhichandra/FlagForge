package com.flagforge.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

public class ApiExceptions {

    @ResponseStatus(HttpStatus.NOT_FOUND)
    public static class FlagNotFoundException extends RuntimeException {
        public FlagNotFoundException(String key) {
            super("Flag not found: " + key);
        }
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    public static class DuplicateFlagKeyException extends RuntimeException {
        public DuplicateFlagKeyException(String key) {
            super("Flag key already exists in this environment: " + key);
        }
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    public static class InsufficientRoleException extends RuntimeException {
        public InsufficientRoleException(String requiredRole) {
            super("Requires role: " + requiredRole);
        }
    }
}
