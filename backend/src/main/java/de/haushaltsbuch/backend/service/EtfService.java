package de.haushaltsbuch.backend.service;

import de.haushaltsbuch.backend.dto.*;
import de.haushaltsbuch.backend.model.Etf;
import de.haushaltsbuch.backend.model.EtfSnapshot;
import de.haushaltsbuch.backend.repository.EtfRepository;
import de.haushaltsbuch.backend.repository.EtfSnapshotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;


/**
 * Business logic for managing ETFs and their value snapshots.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class EtfService {

    private final EtfRepository etfRepository;
    private final EtfSnapshotRepository snapshotRepository;

    /**
     * Returns all ETFs, each enriched with its most recent snapshot.
     *
     * @return list of ETF responses ordered by database insertion order
     */
    public List<EtfResponse> getAll() {
        return etfRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Returns a single ETF by ID, enriched with its most recent snapshot.
     *
     * @param id the ETF ID
     * @return the ETF response
     * @throws ResponseStatusException {@code 404 Not Found} when no ETF with that ID exists
     */
    public EtfResponse getById(Long id) {
        Etf etf = findEtfOrThrow(id);
        return toResponse(etf);
    }

    /**
     * Creates and persists a new ETF.
     *
     * @param request the ETF data from the API caller
     * @return the saved ETF (without snapshot, as none exist yet)
     * @throws ResponseStatusException {@code 409 Conflict} when an ETF with the same WKN already exists
     */
    public EtfResponse create(EtfRequest request) {
        if (etfRepository.findByWkn(request.wkn().toUpperCase()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "An ETF with WKN '" + request.wkn() + "' already exists");
        }

        Etf etf = Etf.builder()
                .name(request.name())
                .wkn(request.wkn())
                .isin(request.isin())
                .brokerName(request.brokerName())
                .notes(request.notes())
                .build();

        return EtfResponse.from(etfRepository.save(etf), null);
    }

    /**
     * Updates an existing ETF's metadata fields.
     *
     * @param id      the ETF ID
     * @param request updated ETF data
     * @return the updated ETF response with its latest snapshot
     * @throws ResponseStatusException {@code 404 Not Found} when no ETF with that ID exists
     * @throws ResponseStatusException {@code 409 Conflict} when the new WKN is already used by a different ETF
     */
    public EtfResponse update(Long id, EtfRequest request) {
        Etf etf = findEtfOrThrow(id);

        // Check WKN uniqueness only if it changed
        String newWkn = request.wkn().trim().toUpperCase();
        if (!newWkn.equals(etf.getWkn())) {
            etfRepository.findByWkn(newWkn).ifPresent(other -> {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "An ETF with WKN '" + newWkn + "' already exists");
            });
        }

        etf.setName(request.name());
        etf.setWkn(request.wkn());
        etf.setIsin(request.isin());
        etf.setBrokerName(request.brokerName());
        etf.setNotes(request.notes());

        return toResponse(etfRepository.save(etf));
    }

    /**
     * Deletes an ETF by ID.
     *
     * <p>Deletion is blocked when the ETF has existing snapshots to prevent
     * accidental data loss.
     *
     * @param id the ETF ID
     * @throws ResponseStatusException {@code 404 Not Found} when the ETF does not exist
     * @throws ResponseStatusException {@code 409 Conflict} when the ETF has snapshots
     */
    public void delete(Long id) {
        Etf etf = findEtfOrThrow(id);

        if (snapshotRepository.existsByEtf(etf)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Cannot delete ETF with existing snapshots");
        }

        etfRepository.delete(etf);
    }

    // ── SNAPSHOT CRUD ─────────────────────────────────────────────────────────

    /**
     * Returns all snapshots for a given ETF, ordered by date ascending.
     *
     * @param etfId the ETF ID
     * @return list of snapshot responses
     * @throws ResponseStatusException {@code 404 Not Found} when the ETF does not exist
     */
    public List<EtfSnapshotResponse> getSnapshots(Long etfId) {
        Etf etf = findEtfOrThrow(etfId);
        return snapshotRepository.findByEtfOrderByDateAsc(etf).stream()
                .map(EtfSnapshotResponse::from)
                .toList();
    }

    /**
     * Adds a new snapshot to an ETF or updates an existing one for the same date.
     *
     * @param etfId   the ETF ID
     * @param request snapshot data
     * @return the saved snapshot response
     * @throws ResponseStatusException {@code 404 Not Found} when the ETF does not exist
     */
    public EtfSnapshotResponse addSnapshot(Long etfId, EtfSnapshotRequest request) {
        Etf etf = findEtfOrThrow(etfId);

        // Upsert: update if a snapshot already exists for this date
        EtfSnapshot snapshot = snapshotRepository
                .findByEtfAndDate(etf, request.date())
                .orElseGet(EtfSnapshot::new);

        snapshot.setEtf(etf);
        snapshot.setDate(request.date());
        snapshot.setTotalValue(request.totalValue());
        snapshot.setShares(request.shares());
        snapshot.setPricePerShare(request.pricePerShare());
        snapshot.setPriceCurrency(request.priceCurrency());
        snapshot.setGainAbsolute(request.gainAbsolute());
        snapshot.setGainPercent(request.gainPercent());

        return EtfSnapshotResponse.from(snapshotRepository.save(snapshot));
    }

    /**
     * Deletes a single snapshot.
     *
     * @param etfId      the ETF ID (used to verify the snapshot belongs to this ETF)
     * @param snapshotId the snapshot ID
     * @throws ResponseStatusException {@code 404 Not Found} when either the ETF or snapshot does not exist,
     *                                 or the snapshot does not belong to the given ETF
     */
    public void deleteSnapshot(Long etfId, Long snapshotId) {
        Etf etf = findEtfOrThrow(etfId);

        EtfSnapshot snapshot = snapshotRepository.findById(snapshotId)
                .filter(s -> s.getEtf().getId().equals(etf.getId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Snapshot not found for ETF " + etfId));

        snapshotRepository.delete(snapshot);
    }

    // ── HELPERS ───────────────────────────────────────────────────────────────

    /**
     * Builds an {@link EtfResponse} for the given ETF by loading its latest snapshot.
     */
    private EtfResponse toResponse(Etf etf) {
        EtfSnapshotResponse latest = snapshotRepository
                .findTopByEtfOrderByDateDesc(etf)
                .map(EtfSnapshotResponse::from)
                .orElse(null);
        return EtfResponse.from(etf, latest);
    }

    /**
     * Loads an ETF by ID or throws {@code 404}.
     */
    private Etf findEtfOrThrow(Long id) {
        return etfRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "ETF not found: " + id));
    }
}
