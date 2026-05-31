package de.haushaltsbuch.backend.dto;

import de.haushaltsbuch.backend.model.ConditionOperator;

import java.util.List;


/**
 * Response DTO for a category rule.
 *
 * @param id                the rule ID
 * @param categoryId        ID of the category assigned on a match
 * @param categoryName      name of the assigned category (for display)
 * @param name              optional human-readable rule label
 * @param priority          matching priority; higher value wins
 * @param active            whether the rule is currently enabled
 * @param conditionOperator how conditions are combined: ALL (AND) or ANY (OR)
 * @param conditions        conditions evaluated according to {@code conditionOperator}
 */
public record CategoryRuleResponse(
        Long id,
        Long categoryId,
        String categoryName,
        String name,
        int priority,
        boolean active,
        ConditionOperator conditionOperator,
        List<RuleConditionResponse> conditions
) {}
