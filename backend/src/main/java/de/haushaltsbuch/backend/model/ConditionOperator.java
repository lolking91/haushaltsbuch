package de.haushaltsbuch.backend.model;

/**
 * Determines how the conditions of a {@link CategoryRule} are combined.
 *
 * <ul>
 *   <li>{@link #ALL} — every condition must match (logical AND, default)</li>
 *   <li>{@link #ANY} — at least one condition must match (logical OR)</li>
 * </ul>
 */
public enum ConditionOperator {
    ALL,
    ANY
}
