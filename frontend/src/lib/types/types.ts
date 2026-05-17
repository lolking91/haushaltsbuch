/** A bank account as returned by GET /api/accounts. */
export type Account = {
	id: number;
	name: string;
	bankName: string;
	iban: string;
	currency: string;
	balance: number;
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

/** Response returned by POST /api/import/ing. */
export type ImportResult = {
	importJobId: number;
	status: string;
	imported: number;
	skipped: number;
	errors: number;
	accountId: number;
};

/** UI state of the CSV import page. */
export type ImportState = 'idle' | 'uploading' | 'success' | 'error';
