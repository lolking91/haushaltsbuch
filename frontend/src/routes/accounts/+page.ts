import { accountsApi } from '$lib/api/accounts.js';
import type { PageLoad } from './$types.js';

export const ssr = false;

export const load: PageLoad = async ({ fetch }) => {
	const accounts = await accountsApi.getAll(fetch);
	return { accounts };
};
