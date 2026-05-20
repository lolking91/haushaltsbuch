package de.haushaltsbuch.backend.dto;

import de.haushaltsbuch.backend.model.ConditionField;
import de.haushaltsbuch.backend.model.ConditionMatcher;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;


/**
 * A single condition inside a {@link CategoryRuleRequest}.
 *
 * @param field   the transaction field to evaluate
 * @param matcher how to compare the field value
 * @param value   the expected value (case-insensitive)
 */
public record RuleConditionRequest(
        @NotNull ConditionField field,
        @NotNull ConditionMatcher matcher,
        @NotBlank String value
) {}
