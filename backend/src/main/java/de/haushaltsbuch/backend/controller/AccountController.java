package de.haushaltsbuch.backend.controller;

import de.haushaltsbuch.backend.dto.AccountRequest;
import de.haushaltsbuch.backend.model.Account;
import de.haushaltsbuch.backend.repository.AccountRepository;
import de.haushaltsbuch.backend.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

/**
 * REST controller for managing accounts.
 */
@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountRepository accountRepository;
    private final AccountService accountService;

    /**
     * Returns all accounts.
     *
     * @return list of all accounts
     */
    @GetMapping
    public List<Account> getAll() {
        return accountRepository.findAll();
    }

    /**
     * Returns a single account by ID.
     *
     * @param id the account ID
     * @return {@code 200 OK} with the account, or {@code 404 Not Found}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Account> getById(@PathVariable Long id) {
        return accountRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Creates a new account.
     *
     * @param request validated account data
     * @return {@code 201 Created} with the saved account and a {@code Location} header
     */
    @PostMapping
    public ResponseEntity<Account> create(@Valid @RequestBody AccountRequest request) {
        Account saved = accountService.create(request);
        URI location = URI.create("/api/accounts/" + saved.getId());
        return ResponseEntity.created(location).body(saved);
    }
}
