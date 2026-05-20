package de.haushaltsbuch.backend.controller;

import de.haushaltsbuch.backend.dto.ApplyRulesResult;
import de.haushaltsbuch.backend.dto.CategoryRuleRequest;
import de.haushaltsbuch.backend.dto.CategoryRuleResponse;
import de.haushaltsbuch.backend.service.CategoryRuleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;


/**
 * REST controller for managing and applying category rules.
 */
@RestController
@RequestMapping("/api/category-rules")
@RequiredArgsConstructor
public class CategoryRuleController {

    private final CategoryRuleService categoryRuleService;

    /**
     * Returns all category rules sorted by priority descending.
     *
     * @return list of all rules with their conditions
     */
    @GetMapping
    public List<CategoryRuleResponse> getAll() {
        return categoryRuleService.getAll();
    }

    /**
     * Returns a single category rule by ID.
     *
     * @param id the rule ID
     * @return {@code 200 OK} with the rule, or {@code 404 Not Found}
     */
    @GetMapping("/{id}")
    public ResponseEntity<CategoryRuleResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(categoryRuleService.getById(id));
    }

    /**
     * Creates a new category rule.
     *
     * @param request validated rule data including at least one condition
     * @return {@code 201 Created} with the saved rule and a {@code Location} header
     */
    @PostMapping
    public ResponseEntity<CategoryRuleResponse> create(@Valid @RequestBody CategoryRuleRequest request) {
        CategoryRuleResponse saved = categoryRuleService.create(request);
        URI location = URI.create("/api/category-rules/" + saved.id());
        return ResponseEntity.created(location).body(saved);
    }

    /**
     * Replaces an existing category rule entirely.
     *
     * <p>All conditions are replaced with the ones in the request body.
     *
     * @param id      the rule ID from the URL path
     * @param request new values for all rule fields
     * @return {@code 200 OK} with the updated rule, or {@code 404 Not Found}
     */
    @PutMapping("/{id}")
    public ResponseEntity<CategoryRuleResponse> update(@PathVariable Long id,
                                                       @Valid @RequestBody CategoryRuleRequest request) {
        return ResponseEntity.ok(categoryRuleService.update(id, request));
    }

    /**
     * Deletes a category rule and all its conditions.
     *
     * @param id the rule ID from the URL path
     * @return {@code 204 No Content} on success, or {@code 404 Not Found}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        categoryRuleService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Applies all active rules to every uncategorized transaction.
     *
     * <p>Transactions that already have a category are not affected.
     *
     * @return {@code 200 OK} with the number of newly categorized transactions
     */
    @PostMapping("/apply")
    public ResponseEntity<ApplyRulesResult> apply() {
        return ResponseEntity.ok(categoryRuleService.applyToAll());
    }
}
