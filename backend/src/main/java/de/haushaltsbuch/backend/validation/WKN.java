package de.haushaltsbuch.backend.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Validates that a WKN (Wertpapierkennnummer) is well-formed.
 *
 * <p>A valid WKN consists of exactly 6 alphanumeric characters (A–Z, 0–9).
 * {@code null} values are accepted (use {@code @NotNull} separately if required).
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = WknValidator.class)
public @interface WKN {
    String message() default "Invalid WKN";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
