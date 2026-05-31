package de.haushaltsbuch.backend.service;

import de.haushaltsbuch.backend.dto.ApplyRulesResult;
import de.haushaltsbuch.backend.dto.CategoryRuleRequest;
import de.haushaltsbuch.backend.dto.CategoryRuleResponse;
import de.haushaltsbuch.backend.dto.RuleConditionResponse;
import de.haushaltsbuch.backend.exception.NotFoundException;
import de.haushaltsbuch.backend.model.*;
import de.haushaltsbuch.backend.repository.CategoryRepository;
import de.haushaltsbuch.backend.repository.CategoryRuleRepository;
import de.haushaltsbuch.backend.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;


/**
 * Business logic for managing and applying category rules.
 *
 * <p>Rules are evaluated in priority order (highest first). The first rule
 * whose every condition matches wins — subsequent rules are not evaluated.
 * Transactions that already have a category are never overwritten.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class CategoryRuleService {

    private final CategoryRuleRepository ruleRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;

    // ── CRUD ─────────────────────────────────────────────────────────────────

    /**
     * Returns all rules sorted by priority descending.
     */
    public List<CategoryRuleResponse> getAll() {
        return ruleRepository.findAll().stream()
                .sorted((a, b) -> Integer.compare(b.getPriority(), a.getPriority()))
                .map(this::toResponse)
                .toList();
    }

    /**
     * Returns a single rule by ID.
     *
     * @param id the rule ID
     * @return the rule response DTO
     * @throws NotFoundException if no rule with the given ID exists
     */
    public CategoryRuleResponse getById(Long id) {
        return ruleRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new NotFoundException("CategoryRule", id));
    }

    /**
     * Creates and persists a new category rule with its conditions.
     *
     * @param request the rule data from the API caller
     * @return the saved rule as a response DTO
     * @throws NotFoundException if the referenced category does not exist
     */
    public CategoryRuleResponse create(CategoryRuleRequest request) {
        Category category = resolveCategory(request.categoryId());

        ConditionOperator operator = request.conditionOperator() != null
                ? request.conditionOperator()
                : ConditionOperator.ALL;

        CategoryRule rule = CategoryRule.builder()
                .category(category)
                .name(request.name())
                .priority(request.priority())
                .active(request.active())
                .conditionOperator(operator)
                .build();

        request.conditions().forEach(c -> rule.getConditions().add(
                RuleCondition.builder()
                        .rule(rule)
                        .field(c.field())
                        .matcher(c.matcher())
                        .value(c.value())
                        .build()
        ));

        return toResponse(ruleRepository.save(rule));
    }

    /**
     * Replaces an existing rule's properties and conditions entirely.
     *
     * <p>The condition list is cleared and rebuilt from the request. Removed
     * conditions are deleted automatically via {@code orphanRemoval = true}.
     *
     * @param id      the ID of the rule to update
     * @param request new values for all rule fields
     * @return the updated rule as a response DTO
     * @throws NotFoundException if the rule or the referenced category does not exist
     */
    public CategoryRuleResponse update(Long id, CategoryRuleRequest request) {
        CategoryRule rule = ruleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("CategoryRule", id));

        rule.setCategory(resolveCategory(request.categoryId()));
        rule.setName(request.name());
        rule.setPriority(request.priority());
        rule.setActive(request.active());
        rule.setConditionOperator(request.conditionOperator() != null
                ? request.conditionOperator()
                : ConditionOperator.ALL);

        // Clear and rebuild; orphanRemoval deletes removed conditions from the DB
        rule.getConditions().clear();
        request.conditions().forEach(c -> rule.getConditions().add(
                RuleCondition.builder()
                        .rule(rule)
                        .field(c.field())
                        .matcher(c.matcher())
                        .value(c.value())
                        .build()
        ));

        return toResponse(ruleRepository.save(rule));
    }

    /**
     * Deletes a rule and all its conditions (cascaded).
     *
     * @param id the ID of the rule to delete
     * @throws NotFoundException if no rule with the given ID exists
     */
    public void delete(Long id) {
        CategoryRule rule = ruleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("CategoryRule", id));
        ruleRepository.delete(rule);
    }

    // ── Rule application ─────────────────────────────────────────────────────

    /**
     * Applies all active rules to every uncategorized transaction.
     *
     * <p>Transactions that already have a category are skipped so that manual
     * assignments are never overwritten.
     *
     * @return a summary with the number of newly categorized transactions
     */
    public ApplyRulesResult applyToAll() {
        List<CategoryRule> activeRules = ruleRepository.findAllByActiveTrueOrderByPriorityDesc();
        List<Transaction> uncategorized = transactionRepository.findAllByCategoryIsNull();

        int categorized = 0;
        for (Transaction tx : uncategorized) {
            if (categorize(tx, activeRules)) {
                transactionRepository.save(tx);
                categorized++;
            }
        }
        return new ApplyRulesResult(categorized);
    }

    /**
     * Loads all active rules for use during import.
     *
     * <p>Called once per import batch so that the rules are not re-fetched for
     * every individual transaction.
     *
     * @return active rules sorted by priority descending
     */
    public List<CategoryRule> loadActiveRules() {
        return ruleRepository.findAllByActiveTrueOrderByPriorityDesc();
    }

    /**
     * Tries to assign a category to {@code tx} based on the provided rules.
     *
     * <p>Skips the transaction if it already has a category. The transaction
     * entity is mutated in place; the caller is responsible for persisting it.
     *
     * @param tx          the transaction to categorize
     * @param activeRules pre-loaded list of active rules, sorted by priority desc
     * @return {@code true} if a matching rule was found and the category was set
     */
    public boolean categorize(Transaction tx, List<CategoryRule> activeRules) {
        if (tx.getCategory() != null) {
            return false;
        }
        Optional<Category> match = firstMatch(tx, activeRules);
        match.ifPresent(tx::setCategory);
        return match.isPresent();
    }

    // ── Internals ─────────────────────────────────────────────────────────────

    /**
     * Returns the category of the first rule (by priority) whose conditions match
     * the transaction according to its {@link ConditionOperator}, or
     * {@link Optional#empty()} if none matched.
     */
    private Optional<Category> firstMatch(Transaction tx, List<CategoryRule> rules) {
        return rules.stream()
                .filter(rule -> rule.getConditionOperator() == ConditionOperator.ALL
                        ? rule.getConditions().stream().allMatch(c -> matches(c, tx))
                        : rule.getConditions().stream().anyMatch(c -> matches(c, tx)))
                .map(CategoryRule::getCategory)
                .findFirst();
    }

    /**
     * Evaluates a single condition against a transaction field.
     *
     * <p>Comparison is always case-insensitive. A {@code null} field value never
     * matches, regardless of the matcher type.
     */
    private boolean matches(RuleCondition condition, Transaction tx) {
        String candidate = switch (condition.getField()) {
            case COUNTERPARTY_NAME -> tx.getCounterpartyName();
            case DESCRIPTION       -> tx.getDescription();
            case TRANSACTION_TYPE  -> tx.getType() != null ? tx.getType().name() : null;
        };
        if (candidate == null) {
            return false;
        }
        return switch (condition.getMatcher()) {
            case EXACT    -> candidate.equalsIgnoreCase(condition.getValue());
            case CONTAINS -> candidate.toLowerCase().contains(condition.getValue().toLowerCase());
        };
    }

    /**
     * Maps a {@link CategoryRule} entity to a {@link CategoryRuleResponse} DTO.
     */
    private CategoryRuleResponse toResponse(CategoryRule rule) {
        List<RuleConditionResponse> conditionResponses = rule.getConditions().stream()
                .map(c -> new RuleConditionResponse(c.getId(), c.getField(), c.getMatcher(), c.getValue()))
                .toList();

        return new CategoryRuleResponse(
                rule.getId(),
                rule.getCategory().getId(),
                rule.getCategory().getName(),
                rule.getName(),
                rule.getPriority(),
                rule.isActive(),
                rule.getConditionOperator(),
                conditionResponses
        );
    }

    /**
     * Looks up a category by ID.
     *
     * @throws NotFoundException if no category with the given ID exists
     */
    private Category resolveCategory(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Category", categoryId));
    }
}
