package de.haushaltsbuch.backend.csv;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * A single transaction row parsed from an ING CSV export file.
 *
 * <p>The {@code amount} field is signed: negative values represent expenses,
 * positive values represent income.
 */
public record IngCsvRecord(
        LocalDate bookingDate,
        LocalDate valueDate,
        String counterpartyName,
        String bookingText,
        String referenceText,
        BigDecimal amount,
        String currency
) {}
