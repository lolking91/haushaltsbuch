import { categoryRulesApi } from '$lib/api/categoryRules.js';
import { categoriesApi } from '$lib/api/categories.js';
import type { PageLoad } from './$types.js';

export const ssr = false;

export const load: PageLoad = async ({ params, fetch }) => {
	const [rule, categories] = await Promise.all([
		categoryRulesApi.getById(Number(params.id), fetch),
		categoriesApi.getAll(fetch)
	]);
	return { rule, categories };
};
