package de.haushaltsbuch.backend.dto;

import jakarta.validation.constraints.NotBlank;


/**
 * Request body for creating a new category.
 *
 * <p>Separates the API contract from the {@code Category} entity to avoid
 * exposing JPA internals and to allow independent validation rules.
 *
 * @param name             display name of the category; must not be blank
 * @param color            optional hex color code for UI display (e.g. {@code "#e74c3c"})
 * @param parentCategoryId optional ID of the parent category for hierarchical grouping
 */
public record CategoryRequest(
        @NotBlank String name,
        String color,
        Long parentCategoryId
) {
}
