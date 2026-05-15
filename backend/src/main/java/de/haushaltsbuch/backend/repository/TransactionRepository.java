package de.haushaltsbuch.backend.repository;

import de.haushaltsbuch.backend.model.Account;
import de.haushaltsbuch.backend.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByAccountId(Long accountId);

    boolean existsByAccountAndBookingDateAndAmountAndDescription(
            Account account, LocalDate bookingDate, BigDecimal amount, String description);
}
