import { transactionsApi } from '$lib/api/transactions.js';
import type { PageLoad } from './$types.js';

export const ssr = false;

export const load: PageLoad = async ({ params, fetch }) => {
	const transaction = await transactionsApi.getById(Number(params.id), fetch);
	return { transaction };
};
