package de.haushaltsbuch.backend.service;

import de.haushaltsbuch.backend.dto.EtfImportResult;
import de.haushaltsbuch.backend.model.Etf;
import de.haushaltsbuch.backend.model.EtfSnapshot;
import de.haushaltsbuch.backend.repository.EtfRepository;
import de.haushaltsbuch.backend.repository.EtfSnapshotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;


/**
 * Parses and imports MLP Bank depot export CSV files.
 *
 * <h2>Expected CSV structure</h2>
 * <pre>
 * RZBK 0752 MLP
 * (empty)
 * Depotbestand
 * (empty)
 * (empty)
 * BIC; MLPBDE61; ;;Datum/Uhrzeit; 01.06.2026/0:21; ;;
 * ...
 * Name;WKN;Stück/Nominal;Fälligkeit;Aktueller Kurs;Kurswert in EUR;Kursgewinn/-verlust in %;Kursgewinn/-verlust in EUR;
 * ISHSIII-CORE MSCI WLD DLA;A0RPWH;24,045 Stk.;;123,44 EUR;2.968,11;80,63;1.324,92;
 * ...
 * (empty line marks end of data)
 * Gesamtkurswert in EUR:; ...
 * </pre>
 *
 * <p>Numbers use German locale (comma as decimal separator, period as thousands separator).
 * Import is idempotent: re-importing the same file updates existing snapshots instead of
 * creating duplicates (upsert on ETF/date unique constraint).
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class EtfImportService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    /** Column indices in the data rows. */
    private static final int COL_NAME          = 0;
    private static final int COL_WKN           = 1;
    private static final int COL_SHARES        = 2;
    // COL_FAELLIGKEIT (3) is unused
    private static final int COL_PRICE         = 4;
    private static final int COL_TOTAL_VALUE   = 5;
    private static final int COL_GAIN_PERCENT  = 6;
    private static final int COL_GAIN_ABSOLUTE = 7;

    private final EtfRepository etfRepository;
    private final EtfSnapshotRepository snapshotRepository;

    /**
     * Imports an MLP Bank depot export CSV file.
     *
     * <p>For each data row:
     * <ol>
     *   <li>Look up the ETF by WKN; create a new one if it does not exist yet.</li>
     *   <li>Find an existing snapshot for the same (ETF, date) pair.</li>
     *   <li>Insert a new snapshot or update the existing one (upsert).</li>
     * </ol>
     *
     * @param input the CSV input stream (UTF-8 or ISO-8859-1)
     * @return a summary with counts for created ETFs, inserted and updated snapshots
     * @throws IOException                if the stream cannot be read
     * @throws ResponseStatusException    {@code 400 Bad Request} if the file format is unrecognizable
     */
    public EtfImportResult importMlpCsv(InputStream input) throws IOException {
        List<String> lines = readLines(input);

        LocalDate importDate = parseImportDate(lines);
        List<String[]> dataRows = parseDataRows(lines);

        int created = 0;
        int inserted = 0;
        int updated = 0;

        for (String[] row : dataRows) {
            String name = row[COL_NAME].trim();
            String wkn  = row[COL_WKN].trim().toUpperCase();

            if (name.isBlank() || wkn.isBlank()) {
                log.warn("Skipping row with missing name or WKN: {}", (Object) row);
                continue;
            }

            // Find or create the ETF
            boolean isNew = etfRepository.findByWkn(wkn).isEmpty();
            Etf etf = etfRepository.findByWkn(wkn).orElseGet(() -> {
                Etf newEtf = new Etf();
                newEtf.setName(name);
                newEtf.setWkn(wkn);
                return etfRepository.save(newEtf);
            });
            if (isNew) created++;

            // Upsert snapshot
            boolean isExisting = snapshotRepository.findByEtfAndDate(etf, importDate).isPresent();
            EtfSnapshot snapshot = snapshotRepository
                    .findByEtfAndDate(etf, importDate)
                    .orElseGet(EtfSnapshot::new);

            snapshot.setEtf(etf);
            snapshot.setDate(importDate);
            snapshot.setTotalValue(parseGermanDecimal(row[COL_TOTAL_VALUE]));
            snapshot.setShares(parseShares(row[COL_SHARES]));

            String[] priceAndCurrency = parsePriceWithCurrency(row[COL_PRICE]);
            snapshot.setPricePerShare(priceAndCurrency[0] != null
                    ? parseGermanDecimal(priceAndCurrency[0]) : null);
            snapshot.setPriceCurrency(priceAndCurrency[1]);

            snapshot.setGainPercent(parseGermanDecimalOrNull(row[COL_GAIN_PERCENT]));
            snapshot.setGainAbsolute(parseGermanDecimalOrNull(row[COL_GAIN_ABSOLUTE]));

            snapshotRepository.save(snapshot);

            if (isExisting) updated++; else inserted++;
        }

        log.info("ETF import complete: date={}, created={}, inserted={}, updated={}",
                importDate, created, inserted, updated);

        return new EtfImportResult(importDate, created, inserted, updated);
    }

    // ── PARSING HELPERS ───────────────────────────────────────────────────────

    /**
     * Reads all lines from the input stream, attempting UTF-8 first.
     * MLP Bank files may use ISO-8859-1 for umlauts (e.g., "Stück").
     */
    private List<String> readLines(InputStream input) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8));
        List<String> lines = new ArrayList<>();
        String line;
        while ((line = reader.readLine()) != null) {
            lines.add(line);
        }
        return lines;
    }

    /**
     * Extracts the statement date from the line containing "Datum/Uhrzeit".
     *
     * <p>Example line: {@code BIC; MLPBDE61; ;;Datum/Uhrzeit; 01.06.2026/0:21; ;;}
     * The token after "Datum/Uhrzeit" is split on "/" to get the date part.
     *
     * @throws ResponseStatusException {@code 400} when the date line is not found
     */
    private LocalDate parseImportDate(List<String> lines) {
        for (String line : lines) {
            if (line.contains("Datum/Uhrzeit")) {
                String[] tokens = line.split(";");
                for (int i = 0; i < tokens.length - 1; i++) {
                    if (tokens[i].trim().equals("Datum/Uhrzeit")) {
                        String datePart = tokens[i + 1].trim().split("/")[0].trim();
                        return LocalDate.parse(datePart, DATE_FORMATTER);
                    }
                }
            }
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Could not find 'Datum/Uhrzeit' in the CSV file. " +
                "Please verify that this is an MLP Bank depot export.");
    }

    /**
     * Finds the data section between the column header row and the first empty line after it.
     *
     * <p>The header row starts with "Name;WKN;".
     *
     * @throws ResponseStatusException {@code 400} when the column header is not found
     */
    private List<String[]> parseDataRows(List<String> lines) {
        int headerIndex = -1;
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).startsWith("Name;WKN;")) {
                headerIndex = i;
                break;
            }
        }

        if (headerIndex < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Could not find data header row (expected 'Name;WKN;...'). " +
                    "Please verify that this is an MLP Bank depot export.");
        }

        List<String[]> rows = new ArrayList<>();
        for (int i = headerIndex + 1; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.isBlank()) break; // end of data section

            String[] columns = line.split(";", -1);
            if (columns.length > COL_GAIN_ABSOLUTE) {
                rows.add(columns);
            }
        }
        return rows;
    }

    /**
     * Parses a German-formatted decimal number.
     *
     * <p>German locale uses period as thousands separator and comma as decimal separator,
     * e.g., {@code "2.327,64"} → {@code 2327.64}.
     */
    private BigDecimal parseGermanDecimal(String value) {
        String normalized = value.trim()
                .replaceAll("\\.", "")   // remove thousands separator
                .replace(",", ".");      // decimal comma → point
        return new BigDecimal(normalized);
    }

    /**
     * Like {@link #parseGermanDecimal(String)}, but returns {@code null} for blank input.
     */
    private BigDecimal parseGermanDecimalOrNull(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return parseGermanDecimal(value);
        } catch (NumberFormatException e) {
            log.warn("Could not parse decimal value '{}', skipping", value);
            return null;
        }
    }

    /**
     * Parses the shares field, stripping the unit suffix.
     *
     * <p>Example: {@code "84,583 Stk."} → {@code 84.583}
     */
    private BigDecimal parseShares(String value) {
        if (value == null || value.isBlank()) return null;
        // Remove the unit suffix " Stk." (and any other trailing text after a space)
        String numericPart = value.trim().split(" ")[0];
        return parseGermanDecimalOrNull(numericPart);
    }

    /**
     * Parses a price-with-currency token into a [numericString, currencyCode] pair.
     *
     * <p>Examples:
     * <ul>
     *   <li>{@code "27,519 EUR"} → {@code ["27,519", "EUR"]}</li>
     *   <li>{@code "163,467 USD"} → {@code ["163,467", "USD"]}</li>
     *   <li>{@code ""} → {@code [null, null]}</li>
     * </ul>
     */
    private String[] parsePriceWithCurrency(String value) {
        if (value == null || value.isBlank()) return new String[]{null, null};
        String[] parts = value.trim().split(" ");
        if (parts.length < 2) return new String[]{parts[0], null};
        return new String[]{parts[0], parts[1]};
    }
}
