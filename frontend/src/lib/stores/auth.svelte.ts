import { authApi } from '$lib/api/auth.js';
import { ApiError } from '$lib/api/client.js';

function createAuth() {
	let username = $state<string | null>(null);
	let checked = $state(false);

	return {
		get username() {
			return username;
		},
		get isAuthenticated() {
			return username !== null;
		},
		/** Whether the initial session check (see {@link checkSession}) has completed. */
		get checked() {
			return checked;
		},
		/** Queries `/api/auth/me` to restore auth state after a page (re)load. */
		async checkSession() {
			try {
				const result = await authApi.me();
				username = result.username;
			} catch (err) {
				if (!(err instanceof ApiError && err.status === 401)) throw err;
				username = null;
			} finally {
				checked = true;
			}
		},
		async login(usernameInput: string, password: string) {
			const result = await authApi.login(usernameInput, password);
			username = result.username;
		},
		async logout() {
			await authApi.logout();
			username = null;
		}
	};
}

export const auth = createAuth();
