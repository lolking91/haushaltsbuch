package de.haushaltsbuch.backend.model;

/**
 * The transaction field a rule condition is matched against.
 */
public enum ConditionField {

    /** The name of the counterparty (Auftraggeber / Empfänger). */
    COUNTERPARTY_NAME,

    /** The booking reference text (Verwendungszweck). */
    DESCRIPTION,

    /** The transaction type: {@code INCOME} or {@code EXPENSE}. */
    TRANSACTION_TYPE
}
