package de.haushaltsbuch.backend.service;

import de.haushaltsbuch.backend.dto.AccountRequest;
import de.haushaltsbuch.backend.model.Account;
import de.haushaltsbuch.backend.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


/**
 * Business logic for managing accounts.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;

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
}
