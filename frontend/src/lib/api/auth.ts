import { api, ApiError } from './client.js';

/** API service for authentication endpoints. */
export const authApi = {
	/** Logs in with username/password. Throws {@link ApiError} (401) on bad credentials. */
	login: (username: string, password: string, customFetch?: typeof fetch): Promise<{ username: string }> =>
		api.request(
			'/api/auth/login',
			{ method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ username, password }) },
			customFetch
		),

	/** Ends the current session. */
	logout: (customFetch?: typeof fetch): Promise<void> =>
		api.request('/api/auth/logout', { method: 'POST' }, customFetch),

	/** Returns the currently logged-in user, or throws {@link ApiError} (401) if there is no session. */
	me: (customFetch?: typeof fetch): Promise<{ username: string }> => api.request('/api/auth/me', undefined, customFetch)
};

export { ApiError };
