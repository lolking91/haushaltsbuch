package de.haushaltsbuch.backend.repository;

import de.haushaltsbuch.backend.model.Account;
import de.haushaltsbuch.backend.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByAccountId(Long accountId);

    boolean existsByAccountAndBookingDateAndAmountAndDescription(
            Account account, LocalDate bookingDate, BigDecimal amount, String description);

    /** Returns all transactions that have not been assigned a category yet. */
    List<Transaction> findAllByCategoryIsNull();

    /**
     * Returns all transactions whose value date falls within the given range, with their
     * category eagerly fetched to avoid N+1 queries during analytics aggregation.
     *
     * @param from start of the date range (inclusive)
     * @param to   end of the date range (inclusive)
     */
    @Query("SELECT t FROM Transaction t LEFT JOIN FETCH t.category WHERE t.valueDate BETWEEN :from AND :to")
    List<Transaction> findForAnalytics(@Param("from") LocalDate from, @Param("to") LocalDate to);
}
