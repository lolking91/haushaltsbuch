package de.haushaltsbuch.backend.dto;

import de.haushaltsbuch.backend.model.ConditionOperator;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;


/**
 * Request body for creating or replacing a category rule.
 *
 * @param categoryId        ID of the category to assign when the rule matches
 * @param name              optional human-readable label, e.g. {@code "Amazon → Shopping"}
 * @param priority          higher value wins when multiple rules match the same transaction; defaults to {@code 0}
 * @param active            when {@code false} the rule is skipped during matching; defaults to {@code true}
 * @param conditionOperator {@link ConditionOperator#ALL} (AND) or {@link ConditionOperator#ANY} (OR);
 *                          defaults to {@code ALL} when omitted
 * @param conditions        at least one condition; combined according to {@code conditionOperator}
 */
public record CategoryRuleRequest(
        @NotNull Long categoryId,
        String name,
        int priority,
        boolean active,
        ConditionOperator conditionOperator,
        @NotEmpty @Valid List<RuleConditionRequest> conditions
) {}
