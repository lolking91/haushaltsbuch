package de.haushaltsbuch.backend.controller;

import de.haushaltsbuch.backend.dto.TransactionRequest;
import de.haushaltsbuch.backend.dto.TransactionUpdateRequest;
import de.haushaltsbuch.backend.model.Transaction;
import de.haushaltsbuch.backend.repository.TransactionRepository;
import de.haushaltsbuch.backend.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;


/**
 * REST controller for managing transactions.
 */
@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionRepository transactionRepository;
    private final TransactionService transactionService;

    /**
     * Returns a single transaction by ID.
     *
     * @param id the transaction ID
     * @return {@code 200 OK} with the transaction, or {@code 404 Not Found}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Transaction> getById(@PathVariable Long id) {
        return transactionRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Returns all transactions, optionally filtered by account.
     *
     * @param accountId optional account filter
     * @return list of matching transactions
     */
    @GetMapping
    public List<Transaction> getAll(@RequestParam(required = false) Long accountId) {
        if (accountId != null) {
            return transactionRepository.findByAccountId(accountId);
        }
        return transactionRepository.findAll();
    }

    /**
     * Creates a new transaction.
     *
     * @param request validated transaction data; account and category are referenced by ID
     * @return {@code 201 Created} with the saved transaction and a {@code Location} header
     */
    @PostMapping
    public ResponseEntity<Transaction> create(@Valid @RequestBody TransactionRequest request) {
        Transaction saved = transactionService.create(request);
        URI location = URI.create("/api/transactions/" + saved.getId());
        return ResponseEntity.created(location).body(saved);
    }

    /**
     * Updates editable fields of an existing transaction.
     *
     * @param id      the transaction ID from the URL path
     * @param request fields to update; {@code null} values clear the respective field
     * @return {@code 200 OK} with the updated transaction, or {@code 404 Not Found}
     */
    @PatchMapping("/{id}")
    public ResponseEntity<Transaction> update(@PathVariable Long id,
                                              @Valid @RequestBody TransactionUpdateRequest request) {
        Transaction updated = transactionService.update(id, request);
        return ResponseEntity.ok(updated);
    }
}
