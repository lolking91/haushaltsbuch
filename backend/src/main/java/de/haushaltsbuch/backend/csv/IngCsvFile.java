package de.haushaltsbuch.backend.csv;

import java.util.List;

/**
 * The complete result of parsing an ING CSV file, combining the account metadata
 * from the header section with the list of individual transaction records.
 */
public record IngCsvFile(
        IngCsvHeader header,
        List<IngCsvRecord> records
) {}
