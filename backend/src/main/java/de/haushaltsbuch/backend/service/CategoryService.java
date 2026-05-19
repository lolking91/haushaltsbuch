package de.haushaltsbuch.backend.service;

import de.haushaltsbuch.backend.dto.CategoryRequest;
import de.haushaltsbuch.backend.dto.CategoryResponse;
import de.haushaltsbuch.backend.exception.NotFoundException;
import de.haushaltsbuch.backend.model.Category;
import de.haushaltsbuch.backend.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


/**
 * Business logic for managing categories.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    /**
     * Returns all categories as response DTOs.
     *
     * @return list of all categories, each with its parent info if present
     */
    public List<CategoryResponse> getAll() {
        return categoryRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Returns a single category by ID.
     *
     * @param id the category ID
     * @return the category response DTO
     * @throws NotFoundException if no category with the given ID exists
     */
    public CategoryResponse getById(Long id) {
        return categoryRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new NotFoundException("Category", id));
    }

    /**
     * Creates and persists a new category.
     *
     * <p>If a {@code parentCategoryId} is provided, the parent is resolved from
     * the database first. This allows building hierarchical category trees
     * (e.g. "Food" → "Groceries", "Food" → "Restaurants").
     *
     * @param request the category data from the API caller
     * @return the saved category as a response DTO
     * @throws NotFoundException if the referenced parent category does not exist
     */
    public CategoryResponse create(CategoryRequest request) {
        Category category = Category.builder()
                .name(request.name())
                .color(request.color())
                .parentCategory(resolveParent(request.parentCategoryId()))
                .build();

        return toResponse(categoryRepository.save(category));
    }

    /**
     * Updates an existing category.
     *
     * @param id      ID of the category to update, taken from the URL path
     * @param request new values for name, color and optional parent
     * @return the updated category as a response DTO
     * @throws NotFoundException if the category or the referenced parent does not exist
     */
    public CategoryResponse update(Long id, CategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Category", id));

        category.setName(request.name());
        category.setColor(request.color());
        category.setParentCategory(resolveParent(request.parentCategoryId()));

        return toResponse(categoryRepository.save(category));
    }

    /**
     * Maps a {@link Category} entity to a {@link CategoryResponse} DTO.
     *
     * <p>Accessing {@code parentCategory} here is safe because this method is
     * always called within an active transaction (the class is {@code @Transactional}).
     *
     * @param category the entity to map
     * @return the corresponding response DTO
     */
    private CategoryResponse toResponse(Category category) {
        CategoryResponse.ParentInfo parent = null;
        if (category.getParentCategory() != null) {
            Category p = category.getParentCategory();
            parent = new CategoryResponse.ParentInfo(p.getId(), p.getName(), p.getColor());
        }
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getColor(),
                parent
        );
    }

    /**
     * Resolves an optional parent category ID to its entity.
     *
     * @param parentCategoryId the ID to look up, or {@code null} for a root category
     * @return the parent {@link Category}, or {@code null} if no ID was given
     * @throws NotFoundException if the ID is set but does not match any category
     */
    private Category resolveParent(Long parentCategoryId) {
        if (parentCategoryId == null) {
            return null;
        }
        return categoryRepository.findById(parentCategoryId)
                .orElseThrow(() -> new NotFoundException("Category", parentCategoryId));
    }
}
