package de.haushaltsbuch.backend.service;

import de.haushaltsbuch.backend.csv.IngCsvFile;
import de.haushaltsbuch.backend.csv.IngCsvHeader;
import de.haushaltsbuch.backend.csv.IngCsvParser;
import de.haushaltsbuch.backend.csv.IngCsvRecord;
import de.haushaltsbuch.backend.dto.ImportResult;
import de.haushaltsbuch.backend.model.*;
import de.haushaltsbuch.backend.repository.AccountRepository;
import de.haushaltsbuch.backend.repository.ImportJobRepository;
import de.haushaltsbuch.backend.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Handles the import of bank statement CSV files.
 *
 * <p>Import flow:
 * <ol>
 *   <li>Parse the CSV file</li>
 *   <li>Find or create the account by IBAN; update the balance from the file header</li>
 *   <li>Persist each transaction, skipping duplicates</li>
 *   <li>Create an {@link ImportJob} record for audit purposes</li>
 * </ol>
 */
@Service
@Transactional
public class ImportService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final ImportJobRepository importJobRepository;
    private final IngCsvParser csvParser;

    public ImportService(AccountRepository accountRepository,
                         TransactionRepository transactionRepository,
                         ImportJobRepository importJobRepository,
                         IngCsvParser csvParser) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.importJobRepository = importJobRepository;
        this.csvParser = csvParser;
    }

    /**
     * Imports an ING bank statement CSV file.
     *
     * @param filename the original file name, stored on the {@link ImportJob}
     * @param input    the CSV input stream
     * @return a summary of the import with counts for imported and skipped transactions
     * @throws IOException if the input stream cannot be read
     */
    public ImportResult importIngCsv(String filename, InputStream input) throws IOException {

        IngCsvFile csvFile = csvParser.parse(input);
        IngCsvHeader header = csvFile.header();

        Account account = accountRepository.findByIban(header.iban())
                .orElseGet(() -> buildAccount(header));

        account.setBalance(header.balance());
        if (account.getCurrency() == null) {
            account.setCurrency(header.currency());
        }
        account = accountRepository.save(account);

        int imported = 0;
        int skipped = 0;

        for (IngCsvRecord record : csvFile.records()) {
            String description = record.referenceText().isEmpty() ? null : record.referenceText();

            if (transactionRepository.existsByAccountAndBookingDateAndAmountAndDescription(
                    account, record.bookingDate(), record.amount(), description)) {
                skipped++;
                continue;
            }

            transactionRepository.save(buildTransaction(record, account, description));
            imported++;
        }

        ImportJob job = new ImportJob();
        job.setFilename(filename);
        job.setImportedAt(LocalDateTime.now());
        job.setStatus(ImportStatus.SUCCESS);
        job.setTransactionCount(imported);
        job.setAccount(account);
        job = importJobRepository.save(job);

        return new ImportResult(job.getId(), "SUCCESS", imported, skipped, account.getId());
    }

    /**
     * Creates a new {@link Account} from the CSV header metadata.
     * The account is not yet persisted; that happens in the calling method.
     */
    private Account buildAccount(IngCsvHeader header) {
        Account account = new Account();
        account.setName(header.accountName());
        account.setBankName(header.bankName());
        account.setIban(header.iban());
        account.setCurrency(header.currency());
        return account;
    }

    /**
     * Maps a parsed CSV row to a {@link Transaction} entity.
     *
     * <p>The transaction type is derived from the sign of the amount:
     * negative → {@code EXPENSE}, zero or positive → {@code INCOME}.
     * The amount is stored as-is (signed) to preserve the original bank data.
     */
    private Transaction buildTransaction(IngCsvRecord record, Account account, String description) {
        TransactionType type = record.amount().compareTo(BigDecimal.ZERO) < 0
                ? TransactionType.EXPENSE
                : TransactionType.INCOME;

        Transaction tx = new Transaction();
        tx.setAmount(record.amount());
        tx.setCurrency(record.currency());
        tx.setBookingDate(record.bookingDate());
        tx.setValueDate(record.valueDate());
        tx.setDescription(description);
        tx.setType(type);
        tx.setCounterpartyName(record.counterpartyName());
        tx.setBookingText(record.bookingText());
        tx.setAccount(account);
        return tx;
    }
}
