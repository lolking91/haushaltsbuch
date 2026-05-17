package de.haushaltsbuch.backend.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.math.BigInteger;
import java.util.stream.Collectors;


public class IbanValidator implements ConstraintValidator<IBAN, String> {

    @Override
    public boolean isValid(String iban, ConstraintValidatorContext context) {
        if (iban == null) return true;

        String normalized = iban.replaceAll("\\s+", "").toUpperCase();

        if (!normalized.matches("[A-Z]{2}\\d{2}[A-Z0-9]{1,30}")) {
            buildMessage(context, normalized, "Invalid IBAN format");
            return false;
        }

        if (!isChecksumValid(normalized)) {
            buildMessage(context, normalized, "Invalid IBAN checksum");
            return false;
        }

        return true;
    }

    private void buildMessage(ConstraintValidatorContext context, String iban, String reason) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(
                reason + ": '" + iban + "'"
        ).addConstraintViolation();
    }

    private boolean isChecksumValid(String iban) {
        String rearranged = iban.substring(4) + iban.substring(0, 4);
        String digits = rearranged.chars()
                .mapToObj(c -> Character.isLetter(c)
                        ? String.valueOf(c - 'A' + 10)
                        : String.valueOf((char) c))
                .collect(Collectors.joining());

        return new BigInteger(digits).mod(BigInteger.valueOf(97)).intValue() == 1;
    }
}
