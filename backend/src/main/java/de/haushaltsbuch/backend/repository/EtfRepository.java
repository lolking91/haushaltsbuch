package de.haushaltsbuch.backend.repository;

import de.haushaltsbuch.backend.model.Etf;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


/**
 * Spring Data repository for {@link Etf} entities.
 */
public interface EtfRepository extends JpaRepository<Etf, Long> {

    /** Finds an ETF by its WKN (case-sensitive; WKNs are stored normalized to uppercase). */
    Optional<Etf> findByWkn(String wkn);
}
