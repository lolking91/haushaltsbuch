package de.haushaltsbuch.backend.dto;

/**
 * Response DTO returned by all category endpoints.
 *
 * <p>The parent is represented as a flat {@link ParentInfo} record, not as a full
 * {@code CategoryResponse}. This prevents infinite nesting
 * (parent → grandparent → …) and keeps the payload predictable.
 *
 * @param id     the category ID
 * @param name   the category name
 * @param color  hex color code, e.g. {@code "#e74c3c"}; may be {@code null}
 * @param parent the direct parent category, or {@code null} for root categories
 */
public record CategoryResponse(
        Long id,
        String name,
        String color,
        ParentInfo parent
) {

    /**
     * Compact parent representation nested inside {@link CategoryResponse}.
     * Intentionally has no own {@code parent} field — nesting depth is always 1.
     *
     * @param id    the parent category ID
     * @param name  the parent category name
     * @param color the parent category hex color; may be {@code null}
     */
    public record ParentInfo(Long id, String name, String color) {}
}
