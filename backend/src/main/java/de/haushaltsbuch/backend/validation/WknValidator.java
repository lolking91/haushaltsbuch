package de.haushaltsbuch.backend.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;


/**
 * Validates the format of a WKN (Wertpapierkennnummer).
 *
 * <p>A WKN must consist of exactly 6 alphanumeric characters (A–Z, 0–9, case-insensitive).
 * {@code null} values pass validation and must be rejected separately with {@code @NotNull}.
 */
public class WknValidator implements ConstraintValidator<WKN, String> {

    private static final String WKN_PATTERN = "[A-Z0-9]{6}";

    @Override
    public boolean isValid(String wkn, ConstraintValidatorContext context) {
        if (wkn == null) return true;

        String normalized = wkn.trim().toUpperCase();

        if (!normalized.matches(WKN_PATTERN)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    "Invalid WKN format (must be 6 alphanumeric characters): '" + wkn + "'"
            ).addConstraintViolation();
            return false;
        }

        return true;
    }
}
