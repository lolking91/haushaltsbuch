import type { Account, AccountRequest } from '$lib/types/types.js';
import { api } from './client.js';

/** API service for account-related endpoints. */
export const accountsApi = {
	/** Fetches all accounts. */
	getAll: (customFetch?: typeof fetch): Promise<Account[]> =>
		api.request('/api/accounts', undefined, customFetch),

	/** Fetches a single account by ID. */
	getById: (id: number, customFetch?: typeof fetch): Promise<Account> =>
		api.request(`/api/accounts/${id}`, undefined, customFetch),

	/** Creates a new account. Returns the saved account with its generated ID. */
	create: (request: AccountRequest, customFetch?: typeof fetch): Promise<Account> =>
		api.request(
			'/api/accounts',
			{ method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(request) },
			customFetch
		)
};
