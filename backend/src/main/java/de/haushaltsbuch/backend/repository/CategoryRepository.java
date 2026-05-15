package de.haushaltsbuch.backend.repository;

import de.haushaltsbuch.backend.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}
