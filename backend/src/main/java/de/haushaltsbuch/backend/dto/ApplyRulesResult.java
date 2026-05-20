package de.haushaltsbuch.backend.dto;


/**
 * Result of a bulk rule-application pass over uncategorized transactions.
 *
 * @param categorized the number of transactions that received a category
 */
public record ApplyRulesResult(int categorized) {}
