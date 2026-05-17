package de.haushaltsbuch.backend.dto;

import de.haushaltsbuch.backend.model.TransactionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;


/**
 * Request body for creating a new transaction.
 *
 * <p>Replaces the {@code Transaction} entity as a {@code @RequestBody} to avoid
 * deserializing lazy JPA associations directly. The referenced {@code Account} and
 * {@code Category} are identified by their IDs and resolved in the service layer.
 *
 * @param amount           signed amount (negative = expense, positive = income)
 * @param currency         ISO 4217 currency code as returned by the bank (e.g. "EUR")
 * @param bookingDate      date the transaction was booked; may be null
 * @param valueDate        value date (Wertstellungsdatum); required
 * @param description      free-text reference / Verwendungszweck; may be null
 * @param type             whether this is an income or expense
 * @param counterpartyName name of the other party
 * @param bookingText      short booking category text from the bank; may be null
 * @param accountId        ID of the account this transaction belongs to
 * @param categoryId       optional ID of the budget category
 */
public record TransactionRequest(
        @NotNull BigDecimal amount,
        @NotBlank String currency,
        LocalDate bookingDate,
        @NotNull LocalDate valueDate,
        String description,
        @NotNull TransactionType type,
        @NotNull String counterpartyName,
        String bookingText,
        @NotNull Long accountId,
        Long categoryId
) {
}
