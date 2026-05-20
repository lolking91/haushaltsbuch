package de.haushaltsbuch.backend.model;

/**
 * How the condition's value is compared to the transaction field.
 */
public enum ConditionMatcher {

    /** The field must equal the value exactly (case-insensitive). */
    EXACT,

    /** The field must contain the value as a substring (case-insensitive). */
    CONTAINS
}
