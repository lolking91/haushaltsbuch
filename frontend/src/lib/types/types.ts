/** A bank account as returned by GET /api/accounts. */
export type Account = {
	id: number;
	name: string;
	bankName: string;
	iban: string;
	currency: 'EUR';
	balance: number;
};

/** Request body for POST /api/accounts. */
export type AccountRequest = {
	name: string;
	bankName: string;
	iban?: string;
	currency: 'EUR';
	balance?: number;
};

/** A single transaction as returned by GET /api/transactions. */
export type Transaction = {
	id: number;
	bookingDate: string;
	valueDate: string;
	counterpartyName: string | null;
	bookingText: string | null;
	description: string | null;
	amount: number;
	currency: string;
	type: 'INCOME' | 'EXPENSE';
	account: Account;
};

/** Request body for POST /api/transactions. */
export type TransactionRequest = {
	amount: number;
	currency: string;
	bookingDate?: string;
	valueDate: string;
	description?: string;
	type: 'INCOME' | 'EXPENSE';
	counterpartyName: string;
	bookingText?: string;
	accountId: number;
	categoryId?: number;
};

/** Request body for PATCH /api/transactions/{id} — only editable fields. */
export type TransactionUpdateRequest = {
	counterpartyName?: string | null;
	description?: string | null;
	bookingText?: string | null;
};

/** Response returned by POST /api/import/ing. */
export type ImportResult = {
	importJobId: number;
	status: string;
	imported: number;
	skipped: number;
	errors: number;
	accountId: number;
};

/** Compact parent info nested inside a {@link Category} response. */
export type CategoryParent = {
	id: number;
	name: string;
	color: string;
};

/** A category as returned by GET /api/categories. */
export type Category = {
	id: number;
	name: string;
	color: string;
	parent: CategoryParent | null;
};

/** Request body for POST /api/categories. */
export type CategoryRequest = {
	name: string;
	color: string; // Hex color code, e.g. "#FF5733"
	parentCategoryId: number | null;
};

/** UI state of the CSV import page. */
export type ImportState = 'idle' | 'uploading' | 'success' | 'error';
