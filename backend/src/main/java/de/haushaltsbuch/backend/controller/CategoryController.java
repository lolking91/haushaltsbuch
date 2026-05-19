package de.haushaltsbuch.backend.controller;

import de.haushaltsbuch.backend.dto.CategoryRequest;
import de.haushaltsbuch.backend.dto.CategoryResponse;
import de.haushaltsbuch.backend.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;


/**
 * REST controller for managing categories.
 */
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * Returns all categories.
     *
     * @return list of all categories, each with parent info if present
     */
    @GetMapping
    public List<CategoryResponse> getAll() {
        return categoryService.getAll();
    }

    /**
     * Returns a single category by ID.
     *
     * @param id the category ID
     * @return {@code 200 OK} with the category, or {@code 404 Not Found}
     */
    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(categoryService.getById(id));
    }

    /**
     * Creates a new category.
     *
     * @param request validated category data
     * @return {@code 201 Created} with the saved category and a {@code Location} header
     */
    @PostMapping
    public ResponseEntity<CategoryResponse> create(@Valid @RequestBody CategoryRequest request) {
        CategoryResponse saved = categoryService.create(request);
        URI location = URI.create("/api/categories/" + saved.id());
        return ResponseEntity.created(location).body(saved);
    }

    /**
     * Updates an existing category.
     *
     * @param id      the category ID from the URL path
     * @param request new values for name, color and optional parent
     * @return {@code 200 OK} with the updated category, or {@code 404 Not Found}
     */
    @PatchMapping("/{id}")
    public ResponseEntity<CategoryResponse> update(@PathVariable Long id,
                                                   @Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.ok(categoryService.update(id, request));
    }
}
