import { transactionsApi } from '$lib/api/transactions.js';
import { categoriesApi } from '$lib/api/categories.js';
import type { PageLoad } from './$types.js';

export const ssr = false;

export const load: PageLoad = async ({ params, fetch }) => {
	const [transaction, categories] = await Promise.all([
		transactionsApi.getById(Number(params.id), fetch),
		categoriesApi.getAll(fetch)
	]);
	return { transaction, categories };
};
