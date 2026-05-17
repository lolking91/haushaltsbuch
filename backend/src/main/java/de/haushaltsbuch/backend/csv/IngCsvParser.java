package de.haushaltsbuch.backend.csv;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Parses ING bank account statements in semicolon-delimited CSV format (Windows-1252 encoding).
 *
 * <p>File structure:
 * <pre>
 *   Metadata header lines  (IBAN, account name, balance, period, ...)
 *   Blank line
 *   Column header row      "Buchung;Wertstellungsdatum;..."
 *   Data rows
 * </pre>
 *
 * <p>Column mapping:
 * <pre>
 *   0  Buchung              → bookingDate
 *   1  Wertstellungsdatum   → valueDate
 *   2  Auftraggeber/Empf.   → counterpartyName
 *   3  Buchungstext         → bookingText
 *   4  Verwendungszweck     → description
 *   5  Saldo                → ignored (running balance after transaction)
 *   6  Währung              → ignored
 *   7  Betrag               → amount (signed: negative = expense)
 *   8  Währung              → currency
 * </pre>
 */
@Component
public class IngCsvParser {

    private static final Logger log = LoggerFactory.getLogger(IngCsvParser.class);
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final String DATA_HEADER_START = "Buchung;";
    // ING exports in Windows-1252, a superset of ISO-8859-1 that covers German umlauts
    private static final Charset ENCODING = Charset.forName("Windows-1252");

    /**
     * Parses an ING CSV input stream into a structured {@link IngCsvFile}.
     *
     * @param input the raw CSV input stream
     * @return parsed header metadata and list of transaction records
     * @throws IOException if the stream cannot be read
     */
    public IngCsvFile parse(InputStream input) throws IOException {
        List<String> lines;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(input, ENCODING))) {
            lines = reader.lines().toList();
        }
        List<IngCsvRecord> records = new ArrayList<>();
        int parseErrors = parseRecords(lines, records);
        return new IngCsvFile(parseHeader(lines), records, parseErrors);
    }

    private IngCsvHeader parseHeader(List<String> lines) {
        String iban = null;
        String accountName = null;
        String bankName = null;
        BigDecimal balance = null;
        String currency = null;
        LocalDate periodFrom = null;
        LocalDate periodTo = null;

        for (String line : lines) {
            if (line.startsWith(DATA_HEADER_START)) break;

            String[] parts = line.split(";", -1);
            if (parts.length < 2) continue;

            switch (parts[0].trim()) {
                case "IBAN"      -> iban        = parts[1].trim().replace(" ", "");
                case "Kontoname" -> accountName = parts[1].trim();
                case "Bank"      -> bankName    = parts[1].trim();
                case "Saldo"     -> {
                    balance  = parseDecimal(parts[1]);
                    currency = parts.length > 2 ? parts[2].trim() : null;
                }
                case "Zeitraum"  -> {
                    String[] period = parts[1].split(" - ", 2);
                    if (period.length == 2) {
                        periodFrom = LocalDate.parse(period[0].trim(), DATE_FORMAT);
                        periodTo   = LocalDate.parse(period[1].trim(), DATE_FORMAT);
                    }
                }
            }
        }

        return new IngCsvHeader(iban, accountName, bankName, balance, currency, periodFrom, periodTo);
    }

    /**
     * Parses the data rows into {@code records} and returns the number of rows that could not be parsed.
     */
    private int parseRecords(List<String> lines, List<IngCsvRecord> records) {
        boolean inData = false;
        int parseErrors = 0;

        for (String line : lines) {
            if (line.startsWith(DATA_HEADER_START)) {
                inData = true;
                continue; // skip the column header row
            }
            if (!inData || line.isBlank()) continue;

            String[] parts = line.split(";", -1);
            if (parts.length < 9) {
                log.warn("Skipping row with too few columns ({}): {}", parts.length, line);
                parseErrors++;
                continue;
            }

            try {
                records.add(new IngCsvRecord(
                        LocalDate.parse(parts[0].trim(), DATE_FORMAT),  // bookingDate
                        LocalDate.parse(parts[1].trim(), DATE_FORMAT),  // valueDate
                        parts[2].trim(),                                 // counterpartyName
                        parts[3].trim(),                                 // bookingText
                        parts[4].trim(),                                 // description (Verwendungszweck)
                        parseDecimal(parts[7]),                          // amount (signed)
                        parts[8].trim()                                  // currency
                ));
            } catch (Exception e) {
                log.warn("Skipping malformed row: {} — {}", e.getMessage(), line);
                parseErrors++;
            }
        }

        return parseErrors;
    }

    /**
     * Converts a German-formatted decimal string (e.g. {@code "14.514,69"} or {@code "-21,42"})
     * to a {@link BigDecimal}. Removes the thousands separator (dot) and replaces the decimal
     * comma with a dot.
     */
    private BigDecimal parseDecimal(String value) {
        String normalized = value.trim()
                .replace(".", "")
                .replace(",", ".");
        return new BigDecimal(normalized);
    }
}
