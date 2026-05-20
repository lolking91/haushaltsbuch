package de.haushaltsbuch.backend.controller;

import de.haushaltsbuch.backend.exception.ConflictException;
import de.haushaltsbuch.backend.exception.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


/**
 * Translates domain exceptions into HTTP responses for all REST controllers.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Maps {@link NotFoundException} to {@code 404 Not Found}.
     *
     * @param ex the exception containing the error message
     * @return response with the exception message as plain-text body
     */
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<String> handleNotFound(NotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    /**
     * Maps {@link ConflictException} to {@code 409 Conflict}.
     *
     * @param ex the exception containing the conflict description
     * @return response with the exception message as plain-text body
     */
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<String> handleConflict(ConflictException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }
}
