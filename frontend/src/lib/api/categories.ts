import type { Category, CategoryRequest } from '$lib/types/types.js';
import { api } from './client.js';

/** API service for category-related endpoints. */
export const categoriesApi = {
	/** Fetches all categories. */
	getAll: (customFetch?: typeof fetch): Promise<Category[]> =>
		api.request('/api/categories', undefined, customFetch),

	/** Fetches a single category by ID. */
	getById: (id: number, customFetch?: typeof fetch): Promise<Category> =>
		api.request(`/api/categories/${id}`, undefined, customFetch),

	/** Creates a new category. Returns the saved category with its generated ID. */
	create: (request: CategoryRequest, customFetch?: typeof fetch): Promise<Category> =>
		api.request(
			'/api/categories',
			{ method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(request) },
			customFetch
		),

	/** Updates name, color and parent of an existing category. */
	update: (id: number, request: CategoryRequest, customFetch?: typeof fetch): Promise<Category> =>
		api.request(
			`/api/categories/${id}`,
			{ method: 'PATCH', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(request) },
			customFetch
		)
};