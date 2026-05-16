import { base } from '$app/paths';

/**
 * Thin fetch wrapper that prepends the SvelteKit base path and throws a
 * descriptive Error for any non-2xx response.
 *
 * @param path  API path starting with "/", e.g. "/api/accounts"
 * @param init  Optional fetch options (method, body, headers, …)
 * @returns     Parsed JSON response body typed as T
 */
async function request<T>(path: string, init?: RequestInit): Promise<T> {
	const res = await fetch(`${base}${path}`, init);

	if (!res.ok) {
		const text = await res.text();
		throw new Error(text || `HTTP ${res.status}`);
	}

	return res.json() as Promise<T>;
}

export const api = { request };
