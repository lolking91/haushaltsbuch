package de.haushaltsbuch.backend.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;


/**
 * Request body for manually adding an ETF snapshot.
 *
 * @param date           date of the snapshot
 * @param totalValue     total market value in EUR
 * @param shares         optional number of shares held
 * @param pricePerShare  optional price per share
 * @param priceCurrency  optional currency of the price-per-share (e.g., "EUR", "USD")
 * @param gainAbsolute   optional cumulative gain/loss in EUR since first purchase
 * @param gainPercent    optional cumulative gain/loss in percent since first purchase
 */
public record EtfSnapshotRequest(
        @NotNull LocalDate date,
        @NotNull BigDecimal totalValue,
        BigDecimal shares,
        BigDecimal pricePerShare,
        String priceCurrency,
        BigDecimal gainAbsolute,
        BigDecimal gainPercent
) {
}
