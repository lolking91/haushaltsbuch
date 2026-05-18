import type { Transaction, TransactionRequest } from '$lib/types/types.js';
import { api } from './client.js';

/** API service for transaction-related endpoints. */
export const transactionsApi = {
	/** Fetches all transactions, optionally filtered by account. */
	getAll: (accountId?: number, customFetch?: typeof fetch): Promise<Transaction[]> =>
		api.request(
			accountId ? `/api/transactions?accountId=${accountId}` : '/api/transactions',
			undefined,
			customFetch
		),

	/** Fetches a single transaction by ID. */
	getById: (id: number, customFetch?: typeof fetch): Promise<Transaction> =>
		api.request(`/api/transactions/${id}`, undefined, customFetch),

	/** Creates a new transaction. Returns the saved transaction with its generated ID. */
	create: (request: TransactionRequest, customFetch?: typeof fetch): Promise<Transaction> =>
		api.request(
			'/api/transactions',
			{ method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(request) },
			customFetch
		)
};
