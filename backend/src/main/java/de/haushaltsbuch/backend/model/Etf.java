package de.haushaltsbuch.backend.model;

import de.haushaltsbuch.backend.validation.WKN;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;


/**
 * Represents an ETF (Exchange Traded Fund) held in the user's portfolio.
 *
 * <p>Each ETF is identified by its WKN (Wertpapierkennnummer). The optional ISIN
 * (International Securities Identification Number) can be added manually.
 * Value history is tracked via {@link EtfSnapshot} entries.
 */
@Entity
@Table(name = "etfs")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@ToString
public class Etf {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Display name as shown in the broker export (e.g., "ISHSIII-CORE MSCI WLD DLA"). */
    @NotBlank
    @Column(nullable = false)
    private String name;

    /** 6-character German security identifier (Wertpapierkennnummer). */
    @NotNull
    @WKN
    @Column(nullable = false, length = 6, unique = true)
    private String wkn;

    /** Optional 12-character International Securities Identification Number. */
    @Column(length = 12)
    private String isin;

    /** Name of the broker holding this ETF (e.g., "MLP Bank"). */
    private String brokerName;

    /** Free-text notes about this ETF. */
    @Column(columnDefinition = "TEXT")
    private String notes;

    /** Normalizes the WKN by trimming whitespace and converting to uppercase. */
    public void setWkn(String wkn) {
        if (wkn != null) {
            this.wkn = wkn.trim().toUpperCase();
        }
    }

    /**
     * Custom Lombok builder that routes {@code wkn()} through the normalizing setter.
     */
    public static class EtfBuilder {
        public EtfBuilder wkn(String wkn) {
            if (wkn != null) {
                this.wkn = wkn.trim().toUpperCase();
            }
            return this;
        }
    }
}
