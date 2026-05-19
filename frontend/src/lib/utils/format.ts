/**
 * Central formatting utilities for display values.
 * All functions are pure (no side-effects) and locale-agnostic in their
 * signatures — the German locale is intentional for this application.
 */

/**
 * Formats a monetary amount as a German-locale currency string.
 *
 * @example formatCurrency(1234.5)        // "1.234,50 €"
 * @example formatCurrency(9.99, 'USD')   // "9,99 $"
 */
export function formatCurrency(amount: number, currency = 'EUR'): string {
	return new Intl.NumberFormat('de-DE', { style: 'currency', currency }).format(amount);
}

/**
 * Formats a date string (YYYY-MM-DD) as a full German short date.
 *
 * @example formatDate('2026-05-14')  // "14.05.2026"
 */
export function formatDate(dateStr: string): string {
	return new Date(dateStr).toLocaleDateString('de-DE', {
		day: '2-digit',
		month: '2-digit',
		year: 'numeric'
	});
}

/**
 * Formats a date string (YYYY-MM-DD) as a short human-readable date without year.
 * Useful for compact lists where the year is implied.
 *
 * @example formatDateShort('2026-05-14')  // "14. Mai"
 */
export function formatDateShort(dateStr: string): string {
	return new Date(dateStr).toLocaleDateString('de-DE', { day: 'numeric', month: 'short' });
}

/**
 * Formats an IBAN with a space every four characters for readability.
 * Returns "—" for null/undefined values.
 *
 * @example formatIban('DE89370400440532013000')  // "DE89 3704 0044 0532 0130 00"
 */
export function formatIban(iban: string | null | undefined): string {
	return iban?.replace(/(.{4})/g, '$1 ').trim() ?? '—';
}
