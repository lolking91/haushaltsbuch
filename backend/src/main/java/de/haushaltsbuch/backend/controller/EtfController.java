package de.haushaltsbuch.backend.controller;

import de.haushaltsbuch.backend.dto.*;
import de.haushaltsbuch.backend.service.EtfService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;


/**
 * REST controller for managing ETFs and their value snapshots.
 */
@RestController
@RequestMapping("/api/etfs")
@RequiredArgsConstructor
public class EtfController {

    private final EtfService etfService;

    // ── ETF endpoints ─────────────────────────────────────────────────────────

    /**
     * Returns all ETFs, each including their most recent snapshot.
     *
     * @return list of ETF responses
     */
    @GetMapping
    public List<EtfResponse> getAll() {
        return etfService.getAll();
    }

    /**
     * Returns a single ETF by ID.
     *
     * @param id the ETF ID
     * @return {@code 200 OK} with the ETF, or {@code 404 Not Found}
     */
    @GetMapping("/{id}")
    public EtfResponse getById(@PathVariable Long id) {
        return etfService.getById(id);
    }

    /**
     * Creates a new ETF.
     *
     * @param request validated ETF data
     * @return {@code 201 Created} with the saved ETF and a {@code Location} header
     */
    @PostMapping
    public ResponseEntity<EtfResponse> create(@Valid @RequestBody EtfRequest request) {
        EtfResponse saved = etfService.create(request);
        URI location = URI.create("/api/etfs/" + saved.id());
        return ResponseEntity.created(location).body(saved);
    }

    /**
     * Updates an existing ETF's metadata.
     *
     * @param id      the ETF ID
     * @param request validated updated ETF data
     * @return {@code 200 OK} with the updated ETF
     */
    @PutMapping("/{id}")
    public EtfResponse update(@PathVariable Long id, @Valid @RequestBody EtfRequest request) {
        return etfService.update(id, request);
    }

    /**
     * Deletes an ETF by ID.
     *
     * <p>Blocked with {@code 409 Conflict} when the ETF still has snapshots.
     *
     * @param id the ETF ID
     * @return {@code 204 No Content} on success
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        etfService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ── Snapshot endpoints ────────────────────────────────────────────────────

    /**
     * Returns all snapshots for a given ETF, ordered by date ascending.
     *
     * @param id the ETF ID
     * @return list of snapshot responses
     */
    @GetMapping("/{id}/snapshots")
    public List<EtfSnapshotResponse> getSnapshots(@PathVariable Long id) {
        return etfService.getSnapshots(id);
    }

    /**
     * Adds a snapshot to an ETF, or updates an existing one for the same date.
     *
     * @param id      the ETF ID
     * @param request validated snapshot data
     * @return {@code 201 Created} with the saved snapshot
     */
    @PostMapping("/{id}/snapshots")
    public ResponseEntity<EtfSnapshotResponse> addSnapshot(
            @PathVariable Long id,
            @Valid @RequestBody EtfSnapshotRequest request) {

        EtfSnapshotResponse saved = etfService.addSnapshot(id, request);
        URI location = URI.create("/api/etfs/" + id + "/snapshots/" + saved.id());
        return ResponseEntity.created(location).body(saved);
    }

    /**
     * Deletes a single snapshot.
     *
     * @param id         the ETF ID
     * @param snapshotId the snapshot ID
     * @return {@code 204 No Content} on success
     */
    @DeleteMapping("/{id}/snapshots/{snapshotId}")
    public ResponseEntity<Void> deleteSnapshot(
            @PathVariable Long id,
            @PathVariable Long snapshotId) {

        etfService.deleteSnapshot(id, snapshotId);
        return ResponseEntity.noContent().build();
    }
}
