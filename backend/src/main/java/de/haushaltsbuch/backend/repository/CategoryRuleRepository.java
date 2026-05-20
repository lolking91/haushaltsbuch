package de.haushaltsbuch.backend.repository;

import de.haushaltsbuch.backend.model.CategoryRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


/**
 * Repository for {@link CategoryRule} entities.
 */
public interface CategoryRuleRepository extends JpaRepository<CategoryRule, Long> {

    /**
     * Returns all active rules sorted by priority descending (highest priority first).
     * Used during rule application so the best-matching rule wins.
     */
    List<CategoryRule> findAllByActiveTrueOrderByPriorityDesc();
}
