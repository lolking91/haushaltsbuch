import type { Etf, EtfRequest, EtfSnapshot, EtfSnapshotRequest, EtfImportResult } from '$lib/types/types.js';
import { api } from './client.js';

/** API service for ETF portfolio endpoints. */
export const etfsApi = {
	// ── ETF CRUD ──────────────────────────────────────────────────────────────

	/** Fetches all ETFs, each including their latest snapshot. */
	getAll: (customFetch?: typeof fetch): Promise<Etf[]> =>
		api.request('/api/etfs', undefined, customFetch),

	/** Fetches a single ETF by ID. */
	getById: (id: number, customFetch?: typeof fetch): Promise<Etf> =>
		api.request(`/api/etfs/${id}`, undefined, customFetch),

	/** Creates a new ETF. Returns the saved ETF with its generated ID. */
	create: (request: EtfRequest, customFetch?: typeof fetch): Promise<Etf> =>
		api.request(
			'/api/etfs',
			{ method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(request) },
			customFetch
		),

	/** Updates an existing ETF. Returns the updated ETF. */
	update: (id: number, request: EtfRequest, customFetch?: typeof fetch): Promise<Etf> =>
		api.request(
			`/api/etfs/${id}`,
			{ method: 'PUT', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(request) },
			customFetch
		),

	/** Deletes an ETF by ID. Throws 409 when the ETF still has snapshots. */
	delete: (id: number, customFetch?: typeof fetch): Promise<void> =>
		api.request(`/api/etfs/${id}`, { method: 'DELETE' }, customFetch),

	// ── Snapshot endpoints ────────────────────────────────────────────────────

	/** Returns all snapshots for a given ETF, ordered by date ascending. */
	getSnapshots: (etfId: number, customFetch?: typeof fetch): Promise<EtfSnapshot[]> =>
		api.request(`/api/etfs/${etfId}/snapshots`, undefined, customFetch),

	/** Adds a snapshot to an ETF, or updates an existing one for the same date. */
	addSnapshot: (etfId: number, request: EtfSnapshotRequest, customFetch?: typeof fetch): Promise<EtfSnapshot> =>
		api.request(
			`/api/etfs/${etfId}/snapshots`,
			{ method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(request) },
			customFetch
		),

	/** Deletes a single snapshot. */
	deleteSnapshot: (etfId: number, snapshotId: number, customFetch?: typeof fetch): Promise<void> =>
		api.request(`/api/etfs/${etfId}/snapshots/${snapshotId}`, { method: 'DELETE' }, customFetch),

	// ── Import ────────────────────────────────────────────────────────────────

	/** Uploads an MLP Bank depot export CSV file. Returns an import summary. */
	importDepot: (file: File, customFetch?: typeof fetch): Promise<EtfImportResult> => {
		const formData = new FormData();
		formData.append('file', file);
		return api.request('/api/import/etf', { method: 'POST', body: formData }, customFetch);
	}
};
