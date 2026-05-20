package de.haushaltsbuch.backend.exception;

/**
 * Thrown when a requested operation would violate a business rule.
 * Mapped to HTTP 409 Conflict by {@link de.haushaltsbuch.backend.controller.GlobalExceptionHandler}.
 */
public class ConflictException extends RuntimeException {

    /**
     * @param message human-readable description of the conflict
     */
    public ConflictException(String message) {
        super(message);
    }
}
