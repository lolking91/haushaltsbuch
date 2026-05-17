package de.haushaltsbuch.backend.model;

import de.haushaltsbuch.backend.validation.IBAN;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;


@Entity
@Table(name = "accounts")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@ToString
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String name;

    @NotBlank
    @Column(nullable = false)
    private String bankName;

    @Column(length = 34)
    @IBAN
    private String iban;

    @NotNull
    @Column(nullable = false, length = 3)
    @Enumerated(EnumType.STRING)
    private Currency currency;

    private BigDecimal balance;

    /** Normalizes the IBAN by removing whitespace and converting to uppercase. */
    public void setIban(String iban) {
        if (iban != null) {
            this.iban = iban.replaceAll("\\s+", "").toUpperCase();
        }
    }

    /**
     * Custom Lombok builder that routes {@code iban()} through the normalizing setter,
     * so {@code Account.builder().iban("DE89 370 …").build()} always stores a clean value.
     */
    public static class AccountBuilder {
        public AccountBuilder iban(String iban) {
            if (iban != null) {
                this.iban = iban.replaceAll("\\s+", "").toUpperCase();
            }
            return this;
        }
    }
}
