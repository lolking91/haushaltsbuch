import { categoriesApi } from '$lib/api/categories.js';
import type { PageLoad } from './$types.js';

export const ssr = false;

export const load: PageLoad = async ({ fetch }) => {
	const categories = await categoriesApi.getAll(fetch);
	return { categories };
};
