package de.haushaltsbuch.backend.controller;

import de.haushaltsbuch.backend.exception.ConflictException;
import de.haushaltsbuch.backend.exception.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;


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

    /**
     * Maps bean-validation failures ({@code @Valid}) to {@code 400 Bad Request}.
     *
     * <p>Returns a JSON object mapping each invalid field name to its first
     * constraint message, e.g. {@code {"iban": "Invalid IBAN checksum: 'DE00...'"}}.
     * The frontend can display these messages inline next to the relevant input.
     *
     * @param ex the validation exception with all field errors
     * @return {@code 400} with a {@code fieldName → message} map as JSON body
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new LinkedHashMap<>();
        for (FieldError err : ex.getBindingResult().getFieldErrors()) {
            // putIfAbsent keeps the first (most specific) error per field
            errors.putIfAbsent(err.getField(), err.getDefaultMessage());
        }
        return ResponseEntity.badRequest().body(errors);
    }
}
