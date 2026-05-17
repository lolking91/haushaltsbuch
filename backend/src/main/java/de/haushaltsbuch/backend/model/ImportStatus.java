package de.haushaltsbuch.backend.model;

public enum ImportStatus {
    PENDING,
    SUCCESS,
    /** Some transactions were imported, but at least one row caused an error. */
    PARTIAL,
    FAILED
}
