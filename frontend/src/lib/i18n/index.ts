import { browser } from '$app/environment';
import { addMessages, init, locale } from 'svelte-i18n';
import de from './de.json';
import en from './en.json';

/** Supported locale codes. */
export type Locale = 'de' | 'en';

const STORAGE_KEY = 'locale';
const DEFAULT_LOCALE: Locale = 'de';

/** Registers all translation catalogues and initialises svelte-i18n. */
export function setupI18n(): void {
	addMessages('de', de);
	addMessages('en', en);

	const saved = browser ? (localStorage.getItem(STORAGE_KEY) as Locale | null) : null;

	init({
		fallbackLocale: DEFAULT_LOCALE,
		initialLocale: saved ?? DEFAULT_LOCALE
	});
}

/**
 * Switches the active locale and persists the choice to localStorage.
 * Calling this updates all reactive `$_()` usages throughout the app.
 */
export function setLocale(newLocale: Locale): void {
	locale.set(newLocale);
	if (browser) localStorage.setItem(STORAGE_KEY, newLocale);
}

export { locale };
