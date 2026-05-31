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
 * Generates a random hex color string, e.g. "#a3f0c2".
 * Useful as a default value for color pickers.
 */
export function randomColor(): string {
	return '#' + Math.floor(Math.random() * 0xffffff).toString(16).padStart(6, '0');
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

/**
 * Validates an IBAN string (mirrors the Java {@code IbanValidator} exactly).
 *
 * Normalizes whitespace and casing before checking, so the user may type
 * "de89 3704 …" and still get a valid result.
 *
 * @param raw  The raw user input. Empty / whitespace-only values are considered
 *             absent and are always accepted (IBAN is an optional field).
 * @returns    {@code null} when valid or absent;
 *             {@code 'format'}   when the structure does not match the IBAN pattern;
 *             {@code 'checksum'} when the mod-97 checksum is wrong.
 */
export function validateIban(raw: string): 'format' | 'checksum' | null {
	const iban = raw.replace(/\s+/g, '').toUpperCase();
	if (!iban) return null;

	// Must be: 2 letters + 2 digits + 1–30 alphanumeric chars
	if (!/^[A-Z]{2}\d{2}[A-Z0-9]{1,30}$/.test(iban)) return 'format';

	// Mod-97 checksum: move first 4 chars to end, map letters to digits (A=10…Z=35)
	const rearranged = iban.slice(4) + iban.slice(0, 4);
	const digits = rearranged
		.split('')
		.map((c) => (/[A-Z]/.test(c) ? String(c.charCodeAt(0) - 55) : c))
		.join('');

	if (BigInt(digits) % 97n !== 1n) return 'checksum';

	return null;
}
