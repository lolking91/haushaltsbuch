package de.haushaltsbuch.backend.dto;

import de.haushaltsbuch.backend.model.ConditionField;
import de.haushaltsbuch.backend.model.ConditionMatcher;


/**
 * A single condition as returned by the category rules API.
 *
 * @param id      the condition ID
 * @param field   the transaction field that is evaluated
 * @param matcher how the comparison is performed
 * @param value   the expected value
 */
public record RuleConditionResponse(
        Long id,
        ConditionField field,
        ConditionMatcher matcher,
        String value
) {}
