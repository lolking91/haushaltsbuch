package de.haushaltsbuch.backend.controller;

import de.haushaltsbuch.backend.dto.EtfImportResult;
import de.haushaltsbuch.backend.dto.ImportResult;
import de.haushaltsbuch.backend.service.EtfImportService;
import de.haushaltsbuch.backend.service.IngImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;


/**
 * REST controller for importing CSV files of all supported formats.
 *
 * <p>All import variants are grouped under {@code /api/import} to provide a
 * consistent API surface regardless of the file format.
 *
 * <ul>
 *   <li>{@code POST /api/import/ing}  – ING bank statement (transactions)</li>
 *   <li>{@code POST /api/import/etf}  – MLP Bank depot export (ETF snapshots)</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/import")
@RequiredArgsConstructor
public class ImportController {

    private final IngImportService ingImportService;
    private final EtfImportService etfImportService;

    /**
     * Imports an ING bank statement CSV file.
     *
     * <p>Request body: {@code multipart/form-data} with a {@code file} field containing the CSV.
     *
     * @param file       the uploaded CSV file
     * @param applyRules when {@code true}, active category rules are applied to each
     *                   newly imported transaction; defaults to {@code false}
     * @return import summary with counts for imported, skipped and categorized transactions
     * @throws IOException if the file cannot be read
     */
    @PostMapping("/ing")
    public ResponseEntity<ImportResult> importIng(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "applyRules", defaultValue = "false") boolean applyRules) throws IOException {

        ImportResult result = ingImportService.importIngCsv(
                file.getOriginalFilename(),
                file.getInputStream(),
                applyRules
        );
        return ResponseEntity.ok(result);
    }

    /**
     * Imports an MLP Bank depot export CSV file.
     *
     * <p>Request body: {@code multipart/form-data} with a {@code file} field containing the CSV.
     * The import is idempotent: uploading the same file twice updates existing snapshots
     * rather than creating duplicates.
     *
     * @param file the uploaded CSV file
     * @return import summary with counts for created ETFs and inserted/updated snapshots
     * @throws IOException if the file cannot be read
     */
    @PostMapping("/etf")
    public ResponseEntity<EtfImportResult> importEtf(
            @RequestParam("file") MultipartFile file) throws IOException {

        EtfImportResult result = etfImportService.importMlpCsv(file.getInputStream());
        return ResponseEntity.ok(result);
    }
}
