import { accountsApi } from '$lib/api/accounts.js';
import { categoriesApi } from '$lib/api/categories.js';
import { transactionsApi } from '$lib/api/transactions.js';
import type { PageLoad } from './$types.js';

export const ssr = false;

export const load: PageLoad = async ({ url, fetch }) => {
	const accountId = url.searchParams.get('accountId');

	const [transactions, accounts, categories] = await Promise.all([
		transactionsApi.getAll(accountId ? Number(accountId) : undefined, fetch),
		accountsApi.getAll(fetch),
		categoriesApi.getAll(fetch)
	]);

	return { transactions, accounts, categories, selectedAccountId: accountId ?? '' };
};
