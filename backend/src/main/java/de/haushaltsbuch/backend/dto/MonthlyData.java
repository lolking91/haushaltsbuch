package de.haushaltsbuch.backend.dto;

import java.math.BigDecimal;


/**
 * Aggregated income and expense totals for a single calendar month.
 *
 * @param month             the month in {@code yyyy-MM} format, e.g. {@code "2024-01"}
 * @param income            total income for the month (always &ge; 0)
 * @param expenses          total expenses for the month (always &ge; 0)
 * @param balance           income minus expenses for the month
 * @param cumulativeBalance running cumulative balance since the first month in the result set
 */
public record MonthlyData(
        String month,
        BigDecimal income,
        BigDecimal expenses,
        BigDecimal balance,
        BigDecimal cumulativeBalance
) {
}
