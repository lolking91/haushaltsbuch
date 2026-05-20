import { categoryRulesApi } from '$lib/api/categoryRules.js';
import type { PageLoad } from './$types.js';

export const ssr = false;

export const load: PageLoad = async ({ fetch }) => {
	const rules = await categoryRulesApi.getAll(fetch);
	return { rules };
};
