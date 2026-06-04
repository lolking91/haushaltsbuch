package de.haushaltsbuch.backend.dto;

import de.haushaltsbuch.backend.model.Etf;


/**
 * API response for an ETF, including the most recent snapshot if one exists.
 *
 * @param id             ETF ID
 * @param name           display name
 * @param wkn            6-character WKN
 * @param isin           optional 12-character ISIN
 * @param brokerName     optional broker name
 * @param notes          optional notes
 * @param latestSnapshot most recent snapshot, or {@code null} if none exists yet
 */
public record EtfResponse(
        Long id,
        String name,
        String wkn,
        String isin,
        String brokerName,
        String notes,
        EtfSnapshotResponse latestSnapshot
) {

    /** Maps an {@link Etf} entity plus its optional latest snapshot to this response record. */
    public static EtfResponse from(Etf etf, EtfSnapshotResponse latestSnapshot) {
        return new EtfResponse(
                etf.getId(),
                etf.getName(),
                etf.getWkn(),
                etf.getIsin(),
                etf.getBrokerName(),
                etf.getNotes(),
                latestSnapshot
        );
    }
}
