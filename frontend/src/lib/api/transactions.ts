import type { Transaction, TransactionRequest } from '$lib/types/types.js';
import { api } from './client.js';

/** API service for transaction-related endpoints. */
export const transactionsApi = {
	/** Fetches all transactions, optionally filtered by account. */
	getAll: (accountId?: number): Promise<Transaction[]> =>
		api.request(accountId ? `/api/transactions?accountId=${accountId}` : '/api/transactions'),

	/** Creates a new transaction. Returns the saved transaction with its generated ID. */
	create: (request: TransactionRequest): Promise<Transaction> =>
		api.request('/api/transactions', {
			method: 'POST',
			headers: { 'Content-Type': 'application/json' },
			body: JSON.stringify(request)
		})
};
