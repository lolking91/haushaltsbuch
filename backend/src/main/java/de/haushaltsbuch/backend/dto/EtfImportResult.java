package de.haushaltsbuch.backend.dto;

import java.time.LocalDate;


/**
 * Summary of an ETF depot CSV import.
 *
 * @param importDate  the statement date parsed from the CSV header
 * @param created     number of new ETFs created during this import
 * @param inserted    number of new snapshots inserted
 * @param updated     number of existing snapshots updated (same ETF, same date)
 */
public record EtfImportResult(
        LocalDate importDate,
        int created,
        int inserted,
        int updated
) {
}
