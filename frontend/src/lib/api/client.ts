import { base } from '$app/paths';
import { browser } from '$app/environment';

/** Thrown by {@link api.request} for any non-2xx HTTP response. */
export class ApiError extends Error {
	readonly status: number;

	constructor(status: number, body: string) {
		super(body || `HTTP ${status}`);
		this.status = status;
	}
}

/** Reads the CSRF token Spring Security exposes via the readable `XSRF-TOKEN` cookie. */
function readCsrfToken(): string | null {
	if (!browser) return null;
	const match = document.cookie.match(/(?:^|;\s*)XSRF-TOKEN=([^;]*)/);
	return match ? decodeURIComponent(match[1]) : null;
}

/**
 * Thin fetch wrapper that prepends the SvelteKit base path and throws an
 * {@link ApiError} for any non-2xx response.
 *
 * <p>Always sends the session cookie ({@code credentials: 'include'}) and, for
 * state-changing requests, the CSRF header Spring Security expects. On a 401
 * for a protected (non-auth) endpoint, redirects to the login page since the
 * session has expired or was never established.
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
	const method = (init?.method ?? 'GET').toUpperCase();
	const headers = new Headers(init?.headers);

	if (method !== 'GET' && method !== 'HEAD') {
		const csrfToken = readCsrfToken();
		if (csrfToken) headers.set('X-XSRF-TOKEN', csrfToken);
	}

	const res = await customFetch(`${base}${path}`, { ...init, headers, credentials: 'include' });

	if (!res.ok) {
		if (res.status === 401 && browser && !path.startsWith('/api/auth')) {
			window.location.href = `${base}/login`;
		}
		const text = await res.text();
		throw new ApiError(res.status, text);
	}

	const text = await res.text();
	return (text ? JSON.parse(text) : undefined) as T;
}

export const api = { request };
