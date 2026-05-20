package de.haushaltsbuch.backend.repository;

import de.haushaltsbuch.backend.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    /**
     * Returns {@code true} if at least one category has the given category as its parent.
     * Used to guard against deleting a parent category that still has children.
     *
     * <p>Spring Data JPA derives this query via property traversal:
     * {@code parentCategory.id = :parentCategoryId}.
     *
     * @param parentCategoryId the ID of the potential parent
     */
    boolean existsByParentCategoryId(Long parentCategoryId);
}
