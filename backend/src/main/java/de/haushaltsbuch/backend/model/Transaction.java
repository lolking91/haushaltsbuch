package de.haushaltsbuch.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@Entity
@Table(
        name = "transactions",
        uniqueConstraints = @UniqueConstraint(columnNames = {"account_id", "bookingDate", "amount", "description"})
)
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private BigDecimal amount;
    private String currency;
    private LocalDate bookingDate;
    private LocalDate valueDate;
    private String description;

    @Enumerated(EnumType.STRING)
    private TransactionType type;

    private String counterpartyName;
    private String bookingText;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;
}
