package de.haushaltsbuch.backend.repository;

import de.haushaltsbuch.backend.model.ImportJob;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImportJobRepository extends JpaRepository<ImportJob, Long> {
}
