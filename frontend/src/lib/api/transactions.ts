import type { Transaction } from '$lib/types/types.js';
import { api } from './client.js';

/** API service for transaction-related endpoints. */
export const transactionsApi = {
	/** Fetches all transactions. */
	getAll: (): Promise<Transaction[]> => api.request('/api/transactions')
};
