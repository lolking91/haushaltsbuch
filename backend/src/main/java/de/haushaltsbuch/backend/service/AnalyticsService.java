package de.haushaltsbuch.backend.service;

import de.haushaltsbuch.backend.dto.AnalyticsResponse;
import de.haushaltsbuch.backend.dto.CategoryData;
import de.haushaltsbuch.backend.dto.MonthlyData;
import de.haushaltsbuch.backend.model.Transaction;
import de.haushaltsbuch.backend.model.TransactionType;
import de.haushaltsbuch.backend.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;


/**
 * Computes aggregated analytics over a configurable date range.
 *
 * <p>All monetary values in the response are expressed as absolute (non-negative) amounts.
 * The sign information (income vs. expense) is conveyed by placing amounts in separate
 * fields rather than using negative numbers.
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AnalyticsService {

    private static final DateTimeFormatter MONTH_FMT = DateTimeFormatter.ofPattern("yyyy-MM");

    /**
     * Display name used for transactions without a category.
     * The colour for this bucket is intentionally not set here — {@code null} is returned
     * so the frontend can apply its own fallback colour.
     */
    private static final String UNCATEGORIZED_LABEL = "Ohne Kategorie";

    private final TransactionRepository transactionRepository;

    /**
     * Returns the full analytics payload for the given date range.
     *
     * @param from start of the period (inclusive), based on {@code valueDate}
     * @param to   end of the period (inclusive), based on {@code valueDate}
     * @return aggregated KPIs, monthly breakdown, and category breakdown
     */
    public AnalyticsResponse getAnalytics(LocalDate from, LocalDate to) {
        List<Transaction> txs = transactionRepository.findForAnalytics(from, to);

        BigDecimal totalIncome = sumAbsolute(txs, TransactionType.INCOME);
        BigDecimal totalExpenses = sumAbsolute(txs, TransactionType.EXPENSE);
        BigDecimal balance = totalIncome.subtract(totalExpenses);

        return new AnalyticsResponse(
                totalIncome,
                totalExpenses,
                balance,
                computeSavingsRate(totalIncome, balance),
                buildMonthlyData(txs, from, to),
                buildCategoryData(txs, TransactionType.EXPENSE)
        );
    }

    /**
     * Sums the absolute amounts of all transactions of the given type.
     */
    private BigDecimal sumAbsolute(List<Transaction> txs, TransactionType type) {
        return txs.stream()
                .filter(t -> t.getType() == type)
                .map(t -> t.getAmount().abs())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Calculates the savings rate as {@code balance / income * 100}, rounded to one decimal
     * place. Returns {@code null} when there is no income to avoid division by zero.
     */
    private BigDecimal computeSavingsRate(BigDecimal totalIncome, BigDecimal balance) {
        if (totalIncome.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        return balance
                .divide(totalIncome, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"))
                .setScale(1, RoundingMode.HALF_UP);
    }

    /**
     * Builds a chronological list of {@link MonthlyData} entries covering every calendar month
     * between {@code from} and {@code to} — including months with no transactions.
     *
     * <p>The {@code cumulativeBalance} field is a running total that resets at the first month
     * of the requested range (i.e. it does not represent the account balance).
     */
    private List<MonthlyData> buildMonthlyData(List<Transaction> txs, LocalDate from, LocalDate to) {
        // Pre-fill all months so gaps (no transactions) still appear in the chart
        Map<String, BigDecimal> incomeMap = new TreeMap<>();
        Map<String, BigDecimal> expenseMap = new TreeMap<>();

        LocalDate cursor = from.withDayOfMonth(1);
        while (!cursor.isAfter(to)) {
            String key = cursor.format(MONTH_FMT);
            incomeMap.put(key, BigDecimal.ZERO);
            expenseMap.put(key, BigDecimal.ZERO);
            cursor = cursor.plusMonths(1);
        }

        // Accumulate transaction amounts by month
        for (Transaction tx : txs) {
            String month = tx.getValueDate().format(MONTH_FMT);
            BigDecimal abs = tx.getAmount().abs();
            if (tx.getType() == TransactionType.INCOME) {
                incomeMap.merge(month, abs, BigDecimal::add);
            } else {
                expenseMap.merge(month, abs, BigDecimal::add);
            }
        }

        // Build result with running cumulative balance
        List<MonthlyData> result = new ArrayList<>(incomeMap.size());
        BigDecimal cumulative = BigDecimal.ZERO;

        for (String month : incomeMap.keySet()) {
            BigDecimal income = incomeMap.get(month);
            BigDecimal expenses = expenseMap.getOrDefault(month, BigDecimal.ZERO);
            BigDecimal monthBal = income.subtract(expenses);
            cumulative = cumulative.add(monthBal);
            result.add(new MonthlyData(month, income, expenses, monthBal, cumulative));
        }

        return result;
    }

    /**
     * Aggregates transaction amounts by category for the given transaction type, sorted by
     * amount descending. Transactions without a category are grouped under
     * {@value #UNCATEGORIZED_LABEL}.
     */
    private List<CategoryData> buildCategoryData(List<Transaction> txs, TransactionType type) {
        Map<String, BigDecimal> amounts = new LinkedHashMap<>();
        // null color signals "uncategorized" to the frontend, which applies its own fallback.
        Map<String, String> colors = new LinkedHashMap<>();

        txs.stream()
                .filter(t -> t.getType() == type)
                .forEach(tx -> {
                    String name;
                    String color;
                    if (tx.getCategory() != null) {
                        name  = tx.getCategory().getName();
                        color = tx.getCategory().getColor();
                    } else {
                        name  = UNCATEGORIZED_LABEL;
                        color = null; // frontend decides the fallback colour
                    }
                    colors.put(name, color);
                    amounts.merge(name, tx.getAmount().abs(), BigDecimal::add);
                });

        return amounts.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .map(e -> new CategoryData(e.getKey(), colors.get(e.getKey()), e.getValue()))
                .toList();
    }
}
