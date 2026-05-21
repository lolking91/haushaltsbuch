package de.haushaltsbuch.backend.dto;

/**
 * Request body for PATCH /api/transactions/{id}.
 *
 * <p>All fields are optional: a {@code null} value clears the field on the entity.
 * The transaction ID is taken from the URL path, not from this record.
 *
 * @param counterpartyName name of the other party; {@code null} clears the value
 * @param description      free-text reference / Verwendungszweck; {@code null} clears the value
 * @param bookingText      short booking category text from the bank; {@code null} clears the value
 * @param categoryId       ID of the category to assign; {@code null} removes the category
 */
public record TransactionUpdateRequest(
        String counterpartyName,
        String description,
        String bookingText,
        Long categoryId
) {
}
