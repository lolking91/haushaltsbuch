package de.haushaltsbuch.backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;


/**
 * A point-in-time record of an ETF's portfolio value.
 *
 * <p>Snapshots are typically created by importing the broker's depot export CSV.
 * Manual entry is also supported. The unique constraint on {@code (etf, date)}
 * ensures at most one snapshot per ETF per day; re-importing the same CSV
 * updates the existing snapshot rather than creating a duplicate.
 *
 * <p>The {@code gainAbsolute} and {@code gainPercent} fields represent the
 * <em>cumulative</em> total return since the ETF was first purchased
 * (as reported by the broker). From these, the cost basis can be derived:
 * {@code costBasis = totalValue - gainAbsolute}.
 */
@Entity
@Table(
        name = "etf_snapshots",
        uniqueConstraints = @UniqueConstraint(columnNames = {"etf_id", "date"})
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@ToString
public class EtfSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The ETF this snapshot belongs to. */
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "etf_id", nullable = false)
    private Etf etf;

    /** Date of the depot statement from which this snapshot was created. */
    @NotNull
    @Column(nullable = false)
    private LocalDate date;

    /** Total market value of the position in EUR. */
    @NotNull
    @Column(nullable = false)
    private BigDecimal totalValue;

    /** Number of shares (Stück) held at the snapshot date. */
    private BigDecimal shares;

    /** Price per share at the snapshot date (may be in a foreign currency). */
    private BigDecimal pricePerShare;

    /** Currency of the price-per-share field (e.g., "EUR" or "USD"). */
    @Column(length = 3)
    private String priceCurrency;

    /**
     * Cumulative gain/loss in EUR since first purchase, as reported by the broker.
     * Negative for a loss.
     */
    private BigDecimal gainAbsolute;

    /**
     * Cumulative gain/loss in percent since first purchase, as reported by the broker.
     * Negative for a loss.
     */
    private BigDecimal gainPercent;
}
