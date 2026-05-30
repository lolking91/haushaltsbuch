import type { AnalyticsResponse } from '$lib/types/types.js';
import { api } from './client.js';

/** API service for the analytics endpoint. */
export const analyticsApi = {
	/**
	 * Fetches aggregated analytics for the given date range.
	 *
	 * @param from ISO date string {@code yyyy-MM-dd} — defaults to first day of current year on the server
	 * @param to   ISO date string {@code yyyy-MM-dd} — defaults to today on the server
	 */
	getAnalytics: (from?: string, to?: string, customFetch?: typeof fetch): Promise<AnalyticsResponse> => {
		const params = new URLSearchParams();
		if (from) params.set('from', from);
		if (to) params.set('to', to);
		const query = params.size > 0 ? `?${params}` : '';
		return api.request(`/api/analytics${query}`, undefined, customFetch);
	}
};
