package de.haushaltsbuch.backend.controller;

import de.haushaltsbuch.backend.dto.ImportResult;
import de.haushaltsbuch.backend.service.ImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * REST controller for importing bank statement files.
 */
@RestController
@RequestMapping("/api/import")
@RequiredArgsConstructor
public class ImportController {

    private final ImportService importService;

    /**
     * Imports an ING bank statement CSV file.
     *
     * <p>{@code POST /api/import/ing}
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

        ImportResult result = importService.importIngCsv(
                file.getOriginalFilename(),
                file.getInputStream(),
                applyRules
        );
        return ResponseEntity.ok(result);
    }
}
