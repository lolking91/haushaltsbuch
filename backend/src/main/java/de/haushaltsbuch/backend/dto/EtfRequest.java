package de.haushaltsbuch.backend.dto;

import de.haushaltsbuch.backend.validation.WKN;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;


/**
 * Request body for creating or updating an ETF.
 *
 * @param name       display name of the ETF
 * @param wkn        6-character WKN; required and validated by {@link WKN}
 * @param isin       optional 12-character ISIN
 * @param brokerName optional name of the broker holding this ETF
 * @param notes      optional free-text notes
 */
public record EtfRequest(
        @NotBlank String name,
        @NotNull @WKN String wkn,
        String isin,
        String brokerName,
        String notes
) {
}
