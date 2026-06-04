package de.haushaltsbuch.backend.dto;

import de.haushaltsbuch.backend.model.EtfSnapshot;

import java.math.BigDecimal;
import java.time.LocalDate;


/**
 * API response for a single ETF snapshot.
 *
 * <p>Avoids exposing JPA internals and the lazy-loaded {@code Etf} relationship
 * by mapping only the scalar fields needed by the frontend.
 *
 * @param id             snapshot ID
 * @param etfId          ID of the owning ETF
 * @param date           snapshot date
 * @param totalValue     total market value in EUR
 * @param shares         number of shares held (optional)
 * @param pricePerShare  price per share (optional)
 * @param priceCurrency  currency of the price per share (optional)
 * @param gainAbsolute   cumulative gain/loss in EUR (optional)
 * @param gainPercent    cumulative gain/loss in percent (optional)
 */
public record EtfSnapshotResponse(
        Long id,
        Long etfId,
        LocalDate date,
        BigDecimal totalValue,
        BigDecimal shares,
        BigDecimal pricePerShare,
        String priceCurrency,
        BigDecimal gainAbsolute,
        BigDecimal gainPercent
) {

    /** Maps an {@link EtfSnapshot} entity to this response record. */
    public static EtfSnapshotResponse from(EtfSnapshot snapshot) {
        return new EtfSnapshotResponse(
                snapshot.getId(),
                snapshot.getEtf().getId(),
                snapshot.getDate(),
                snapshot.getTotalValue(),
                snapshot.getShares(),
                snapshot.getPricePerShare(),
                snapshot.getPriceCurrency(),
                snapshot.getGainAbsolute(),
                snapshot.getGainPercent()
        );
    }
}
