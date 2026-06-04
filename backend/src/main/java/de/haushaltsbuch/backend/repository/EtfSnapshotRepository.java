package de.haushaltsbuch.backend.repository;

import de.haushaltsbuch.backend.model.Etf;
import de.haushaltsbuch.backend.model.EtfSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;


/**
 * Spring Data repository for {@link EtfSnapshot} entities.
 */
public interface EtfSnapshotRepository extends JpaRepository<EtfSnapshot, Long> {

    /** Returns all snapshots for a given ETF, ordered by date ascending. */
    List<EtfSnapshot> findByEtfOrderByDateAsc(Etf etf);

    /** Returns the most recent snapshot for a given ETF. */
    Optional<EtfSnapshot> findTopByEtfOrderByDateDesc(Etf etf);

    /** Finds a snapshot for a specific ETF and date (used for duplicate-check during import). */
    Optional<EtfSnapshot> findByEtfAndDate(Etf etf, LocalDate date);

    /** Returns {@code true} if any snapshots exist for the given ETF (used to block ETF deletion). */
    boolean existsByEtf(Etf etf);
}
