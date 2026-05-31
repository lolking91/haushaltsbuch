package de.haushaltsbuch.backend.service;

import de.haushaltsbuch.backend.dto.AccountRequest;
import de.haushaltsbuch.backend.model.Account;
import de.haushaltsbuch.backend.repository.AccountRepository;
import de.haushaltsbuch.backend.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;


/**
 * Business logic for managing accounts.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    /**
     * Creates and persists a new account.
     *
     * <p>The IBAN is normalized (whitespace removed, uppercased) by the
     * {@link Account} builder before persisting.
     *
     * @param request the account data from the API caller
     * @return the saved account with its generated ID
     */
    public Account create(AccountRequest request) {
        Account account = Account.builder()
                .name(request.name())
                .bankName(request.bankName())
                .iban(request.iban())
                .currency(request.currency())
                .balance(request.balance())
                .build();

        return accountRepository.save(account);
    }

    /**
     * Updates an existing account's fields.
     *
     * @param id      the account ID
     * @param request updated account data
     * @return the saved account
     * @throws ResponseStatusException {@code 404 Not Found} when no account with that ID exists
     */
    public Account update(Long id, AccountRequest request) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        account.setName(request.name());
        account.setBankName(request.bankName());
        account.setIban(request.iban());
        account.setCurrency(request.currency());
        account.setBalance(request.balance());

        return accountRepository.save(account);
    }

    /**
     * Deletes an account by ID.
     *
     * <p>Deletion is blocked when the account still has linked transactions to
     * preserve referential integrity at the application level.
     *
     * @param id the account ID
     * @throws ResponseStatusException {@code 404 Not Found} when the account does not exist,
     *                                 {@code 409 Conflict} when the account has transactions
     */
    public void delete(Long id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (transactionRepository.existsByAccountId(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Cannot delete account with existing transactions");
        }

        accountRepository.delete(account);
    }
}
