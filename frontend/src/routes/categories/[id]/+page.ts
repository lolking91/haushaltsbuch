import { categoriesApi } from '$lib/api/categories.js';
import type { PageLoad } from './$types.js';

export const ssr = false;

export const load: PageLoad = async ({ params, fetch }) => {
	// Load the category and the full list in parallel.
	// The full list is needed for the parent-category dropdown in the edit form.
	const [category, categories] = await Promise.all([
		categoriesApi.getById(Number(params.id), fetch),
		categoriesApi.getAll(fetch)
	]);
	return { category, categories };
};
