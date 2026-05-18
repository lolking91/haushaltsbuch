import { accountsApi } from '$lib/api/accounts.js';
import { transactionsApi } from '$lib/api/transactions.js';
import type { PageLoad } from './$types.js';

export const ssr = false;

export const load: PageLoad = async ({ url, fetch }) => {
	const accountId = url.searchParams.get('accountId');

	const [transactions, accounts] = await Promise.all([
		transactionsApi.getAll(accountId ? Number(accountId) : undefined, fetch),
		accountsApi.getAll(fetch)
	]);

	return { transactions, accounts, selectedAccountId: accountId ?? '' };
};
