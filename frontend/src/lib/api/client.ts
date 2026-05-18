import { base } from '$app/paths';

/**
 * Thin fetch wrapper that prepends the SvelteKit base path and throws a
 * descriptive Error for any non-2xx response.
 *
 * @param path         API path starting with "/", e.g. "/api/accounts"
 * @param init         Optional fetch options (method, body, headers, …)
 * @param customFetch  The fetch function to use — pass the one from the SvelteKit
 *                     load context so Vite's dev proxy is used correctly.
 *                     Falls back to the global fetch for use outside load functions.
 * @returns            Parsed JSON response body typed as T
 */
async function request<T>(
	path: string,
	init?: RequestInit,
	customFetch: typeof fetch = fetch
): Promise<T> {
	const res = await customFetch(`${base}${path}`, init);

	if (!res.ok) {
		const text = await res.text();
		throw new Error(text || `HTTP ${res.status}`);
	}

	return res.json() as Promise<T>;
}

export const api = { request };
