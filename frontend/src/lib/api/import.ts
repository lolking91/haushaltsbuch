import type { ImportResult } from '$lib/types/types.js';
import { api } from './client.js';

/** API service for import-related endpoints. */
export const importApi = {
	/**
	 * Uploads an ING CSV file and returns the import result.
	 *
	 * @param file  The CSV file selected by the user
	 */
	importIng: (file: File): Promise<ImportResult> => {
		const body = new FormData();
		body.append('file', file);
		return api.request('/api/import/ing', { method: 'POST', body });
	}
};
