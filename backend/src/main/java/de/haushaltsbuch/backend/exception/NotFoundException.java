package de.haushaltsbuch.backend.exception;

/**
 * Thrown when a requested entity does not exist in the database.
 * Mapped to HTTP 404 by {@link de.haushaltsbuch.backend.controller.GlobalExceptionHandler}.
 */
public class NotFoundException extends RuntimeException {

    /**
     * @param entity the entity type (e.g. "Account", "Category")
     * @param id     the requested ID that was not found
     */
    public NotFoundException(String entity, Long id) {
        super(entity + " with id " + id + " not found");
    }
}
