package de.haushaltsbuch.backend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;


/**
 * Request body for creating or replacing a category rule.
 *
 * @param categoryId ID of the category to assign when the rule matches
 * @param name       optional human-readable label, e.g. {@code "Amazon → Shopping"}
 * @param priority   higher value wins when multiple rules match the same transaction; defaults to {@code 0}
 * @param active     when {@code false} the rule is skipped during matching; defaults to {@code true}
 * @param conditions at least one condition; all must match (logical AND)
 */
public record CategoryRuleRequest(
        @NotNull Long categoryId,
        String name,
        int priority,
        boolean active,
        @NotEmpty @Valid List<RuleConditionRequest> conditions
) {}
