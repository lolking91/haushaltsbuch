package de.haushaltsbuch.backend.dto;

import java.math.BigDecimal;


/**
 * Total transaction amount aggregated by category.
 *
 * @param categoryName display name of the category; {@code null} for uncategorized transactions
 * @param color        hex colour code (e.g. {@code "#FF5733"}) associated with the category;
 *                     {@code null} for uncategorized transactions — the frontend applies its own fallback
 * @param amount       aggregated absolute amount (always &ge; 0)
 */
public record CategoryData(String categoryName, String color, BigDecimal amount) {
}
