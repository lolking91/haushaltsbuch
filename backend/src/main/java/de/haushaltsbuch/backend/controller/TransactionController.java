package de.haushaltsbuch.backend.controller;

import de.haushaltsbuch.backend.dto.TransactionRequest;
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
}
