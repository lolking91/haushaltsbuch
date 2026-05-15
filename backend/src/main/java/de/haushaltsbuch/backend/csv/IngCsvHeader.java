package de.haushaltsbuch.backend.csv;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Metadata extracted from the header section of an ING CSV export file.
 * Contains account information and the current balance as of the export date.
 */
public record IngCsvHeader(
        String iban,
        String accountName,
        String bankName,
        BigDecimal balance,
        String currency,
        LocalDate periodFrom,
        LocalDate periodTo
) {}
