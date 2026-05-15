package de.haushaltsbuch.backend.controller;

import de.haushaltsbuch.backend.model.Transaction;
import de.haushaltsbuch.backend.repository.TransactionRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionRepository transactionRepository;

    @GetMapping
    public List<Transaction> getAll(@RequestParam(required = false) Long accountId) {
        if (accountId != null) {
            return transactionRepository.findByAccountId(accountId);
        }
        return transactionRepository.findAll();
    }

    @PostMapping
    public ResponseEntity<Transaction> create(@Valid @RequestBody Transaction transaction) {
        return ResponseEntity.ok(transactionRepository.save(transaction));
    }
}
