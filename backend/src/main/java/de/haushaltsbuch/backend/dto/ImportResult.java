package de.haushaltsbuch.backend.dto;

/**
 * Response returned after a CSV import operation.
 *
 * @param importJobId  the ID of the created {@code ImportJob} record
 * @param status       {@code "SUCCESS"}, {@code "PARTIAL"} or {@code "FAILED"}
 * @param imported     number of transactions successfully imported
 * @param skipped      number of transactions skipped as duplicates
 * @param errors       number of transactions with errors
 * @param accountId    ID of the account the transactions were imported into
 * @param categorized  number of imported transactions that were auto-categorized by rules;
 *                     {@code 0} when {@code applyRules} was {@code false}
 */
public record ImportResult(
        Long importJobId,
        String status,
        int imported,
        int skipped,
        int errors,
        Long accountId,
        int categorized
) {}
