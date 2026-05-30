package de.haushaltsbuch.backend.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Full analytics payload returned by {@code GET /api/analytics}.
 *
 * @param totalIncome      sum of all income transactions in the requested period
 * @param totalExpenses    sum of all expense transactions (positive value) in the period
 * @param balance          totalIncome minus totalExpenses
 * @param savingsRate      balance expressed as a percentage of income (one decimal place);
 *                         {@code null} if there is no income in the period
 * @param monthlyData      income / expense / balance breakdown per calendar month,
 *                         sorted chronologically; every month in the requested range is included
 *                         even if it has no transactions
 * @param categoryExpenses expense totals per category, sorted by amount descending;
 *                         uncategorized transactions are grouped under a dedicated entry
 */
public record AnalyticsResponse(
        BigDecimal totalIncome,
        BigDecimal totalExpenses,
        BigDecimal balance,
        BigDecimal savingsRate,
        List<MonthlyData> monthlyData,
        List<CategoryData> categoryExpenses
) {}
