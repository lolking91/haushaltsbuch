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

	// Read all filter params so the component can restore its state on load.
	return {
		transactions,
		accounts,
		categories,
		params: {
			accountId:      accountId ?? '',
			q:              url.searchParams.get('q') ?? '',
			type:           url.searchParams.get('type') ?? 'ALL',
			categories:     url.searchParams.get('categories') ?? '',
			uncategorized:  url.searchParams.get('uncategorized') === 'true',
			from:           url.searchParams.get('from') ?? '',
			to:             url.searchParams.get('to') ?? '',
			amountMin:      url.searchParams.get('amountMin') ?? '',
			amountMax:      url.searchParams.get('amountMax') ?? '',
		}
	};
};
