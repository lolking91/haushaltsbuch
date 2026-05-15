package de.haushaltsbuch.backend.controller;

import de.haushaltsbuch.backend.dto.ImportResult;
import de.haushaltsbuch.backend.service.ImportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * REST controller for importing bank statement files.
 */
@RestController
@RequestMapping("/api/import")
public class ImportController {

    private final ImportService importService;

    public ImportController(ImportService importService) {
        this.importService = importService;
    }

    /**
     * Imports an ING bank statement CSV file.
     *
     * <p>{@code POST /banking/api/import/ing}
     * <p>Request body: {@code multipart/form-data} with a {@code file} field containing the CSV.
     *
     * @param file the uploaded CSV file
     * @return import summary with counts for imported and skipped transactions
     * @throws IOException if the file cannot be read
     */
    @PostMapping("/ing")
    public ResponseEntity<ImportResult> importIng(
            @RequestParam("file") MultipartFile file) throws IOException {

        ImportResult result = importService.importIngCsv(
                file.getOriginalFilename(),
                file.getInputStream()
        );
        return ResponseEntity.ok(result);
    }
}
