import { accountsApi } from '$lib/api/accounts.js';
import type { PageLoad } from './$types.js';

export const ssr = false;

export const load: PageLoad = async ({ params, fetch }) => {
	const account = await accountsApi.getById(Number(params.id), fetch);
	return { account };
};
