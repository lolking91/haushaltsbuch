import { analyticsApi } from '$lib/api/analytics.js';
import type { PageLoad } from './$types.js';

export const ssr = false;

/** Loads the initial analytics data for the current year. */
export const load: PageLoad = async ({ fetch }) => {
	const now  = new Date();
	const from = `${now.getFullYear()}-01-01`;
	const to   = now.toISOString().slice(0, 10);

	const analytics = await analyticsApi.getAnalytics(from, to, fetch);
	return { analytics, defaultFrom: from, defaultTo: to };
};
