package de.haushaltsbuch.backend.dto;

import de.haushaltsbuch.backend.model.Currency;
import de.haushaltsbuch.backend.validation.IBAN;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;


/**
 * Request body for creating a new account.
 *
 * <p>Separates the API contract from the {@code Account} entity to avoid
 * exposing JPA internals and to allow independent validation rules.
 *
 * @param name     display name of the account
 * @param bankName name of the bank
 * @param iban     optional IBAN; validated by {@link IBAN} if present
 * @param currency account currency
 * @param balance  optional initial balance
 */
public record AccountRequest(
        @NotBlank String name,
        @NotBlank String bankName,
        @IBAN String iban,
        @NotNull Currency currency,
        BigDecimal balance
) {
}
