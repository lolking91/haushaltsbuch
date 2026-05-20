import type { ApplyRulesResult, CategoryRule, CategoryRuleRequest } from '$lib/types/types.js';
import { api } from './client.js';

/** API service for category rule endpoints. */
export const categoryRulesApi = {
	/** Fetches all rules, sorted by priority descending. */
	getAll: (customFetch?: typeof fetch): Promise<CategoryRule[]> =>
		api.request('/api/category-rules', undefined, customFetch),

	/** Fetches a single rule by ID. */
	getById: (id: number, customFetch?: typeof fetch): Promise<CategoryRule> =>
		api.request(`/api/category-rules/${id}`, undefined, customFetch),

	/** Creates a new rule. Returns the saved rule with its generated ID. */
	create: (request: CategoryRuleRequest, customFetch?: typeof fetch): Promise<CategoryRule> =>
		api.request('/api/category-rules', {
			method: 'POST',
			headers: { 'Content-Type': 'application/json' },
			body: JSON.stringify(request)
		}, customFetch),

	/** Replaces an existing rule and all its conditions. */
	update: (id: number, request: CategoryRuleRequest, customFetch?: typeof fetch): Promise<CategoryRule> =>
		api.request(`/api/category-rules/${id}`, {
			method: 'PUT',
			headers: { 'Content-Type': 'application/json' },
			body: JSON.stringify(request)
		}, customFetch),

	/** Deletes a rule and all its conditions. */
	delete: (id: number, customFetch?: typeof fetch): Promise<void> =>
		api.request(`/api/category-rules/${id}`, { method: 'DELETE' }, customFetch),

	/** Applies all active rules to every uncategorized transaction. */
	apply: (customFetch?: typeof fetch): Promise<ApplyRulesResult> =>
		api.request('/api/category-rules/apply', { method: 'POST' }, customFetch)
};
