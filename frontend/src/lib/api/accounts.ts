import type { Account, AccountRequest } from '$lib/types/types.js';
import { api } from './client.js';

/** API service for account-related endpoints. */
export const accountsApi = {
	/** Fetches all accounts. */
	getAll: (): Promise<Account[]> => api.request('/api/accounts'),

	/** Fetches a single account by ID. */
	getById: (id: number): Promise<Account> => api.request(`/api/accounts/${id}`),

	/** Creates a new account. Returns the saved account with its generated ID. */
	create: (request: AccountRequest): Promise<Account> =>
		api.request('/api/accounts', {
			method: 'POST',
			headers: { 'Content-Type': 'application/json' },
			body: JSON.stringify(request)
		})
};
