package de.haushaltsbuch.backend.dto;

import java.util.List;


/**
 * Response DTO for a category rule.
 *
 * @param id           the rule ID
 * @param categoryId   ID of the category assigned on a match
 * @param categoryName name of the assigned category (for display)
 * @param name         optional human-readable rule label
 * @param priority     matching priority; higher value wins
 * @param active       whether the rule is currently enabled
 * @param conditions   all conditions that must match (logical AND)
 */
public record CategoryRuleResponse(
        Long id,
        Long categoryId,
        String categoryName,
        String name,
        int priority,
        boolean active,
        List<RuleConditionResponse> conditions
) {}
