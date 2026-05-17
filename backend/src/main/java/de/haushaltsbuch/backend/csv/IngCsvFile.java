package de.haushaltsbuch.backend.csv;

import java.util.List;

/**
 * The complete result of parsing an ING CSV file, combining the account metadata
 * from the header section with the list of individual transaction records.
 *
 * @param header      account metadata extracted from the CSV header
 * @param records     successfully parsed transaction rows
 * @param parseErrors number of rows that could not be parsed (malformed data)
 */
public record IngCsvFile(
        IngCsvHeader header,
        List<IngCsvRecord> records,
        int parseErrors
) {}
