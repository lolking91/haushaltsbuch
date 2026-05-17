package de.haushaltsbuch.backend.service;

import de.haushaltsbuch.backend.dto.TransactionRequest;
import de.haushaltsbuch.backend.exception.NotFoundException;
import de.haushaltsbuch.backend.model.Account;
import de.haushaltsbuch.backend.model.Category;
import de.haushaltsbuch.backend.model.Transaction;
import de.haushaltsbuch.backend.repository.AccountRepository;
import de.haushaltsbuch.backend.repository.CategoryRepository;
import de.haushaltsbuch.backend.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


/**
 * Business logic for managing transactions.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;

    /**
     * Creates and persists a new transaction.
     *
     * @param request the transaction data from the API caller
     * @return the saved transaction with its generated ID
     * @throws NotFoundException if the referenced account or category does not exist
     */
    public Transaction create(TransactionRequest request) {
        Account account = accountRepository.findById(request.accountId())
                .orElseThrow(() -> new NotFoundException("Account", request.accountId()));

        Category category = null;
        if (request.categoryId() != null) {
            category = categoryRepository.findById(request.categoryId())
                    .orElseThrow(() -> new NotFoundException("Category", request.categoryId()));
        }

        Transaction tx = Transaction.builder()
                .amount(request.amount())
                .currency(request.currency())
                .bookingDate(request.bookingDate())
                .valueDate(request.valueDate())
                .description(request.description())
                .type(request.type())
                .counterpartyName(request.counterpartyName())
                .bookingText(request.bookingText())
                .account(account)
                .category(category)
                .build();

        return transactionRepository.save(tx);
    }
}
