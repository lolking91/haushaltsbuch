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
	category: Category | null;
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
	categoryId?: number | null;
};

/** Response returned by POST /api/import/ing. */
export type ImportResult = {
	importJobId: number;
	status: string;
	imported: number;
	skipped: number;
	errors: number;
	accountId: number;
	/** Number of imported transactions that were auto-categorized; 0 when applyRules was false. */
	categorized: number;
};

/** Which transaction field a rule condition is evaluated against. */
export type ConditionField = 'COUNTERPARTY_NAME' | 'DESCRIPTION' | 'TRANSACTION_TYPE';

/** How the condition value is compared to the transaction field. */
export type ConditionMatcher = 'EXACT' | 'CONTAINS';

/** A single condition within a category rule as returned by the API. */
export type RuleCondition = {
	id: number;
	field: ConditionField;
	matcher: ConditionMatcher;
	value: string;
};

/** A category rule as returned by GET /api/category-rules. */
export type CategoryRule = {
	id: number;
	categoryId: number;
	categoryName: string;
	name: string | null;
	priority: number;
	active: boolean;
	conditions: RuleCondition[];
};

/** Request body for POST/PUT /api/category-rules — single condition. */
export type RuleConditionRequest = {
	field: ConditionField;
	matcher: ConditionMatcher;
	value: string;
};

/** Request body for POST/PUT /api/category-rules. */
export type CategoryRuleRequest = {
	categoryId: number;
	name: string | null;
	priority: number;
	active: boolean;
	conditions: RuleConditionRequest[];
};

/** Result of POST /api/category-rules/apply. */
export type ApplyRulesResult = {
	categorized: number;
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

// ── Analytics ─────────────────────────────────────────────────────────────────

/** Income and expense totals for a single calendar month. */
export type MonthlyData = {
	/** Month in {@code yyyy-MM} format, e.g. {@code "2024-01"}. */
	month: string;
	income: number;
	expenses: number;
	/** income − expenses for this month. */
	balance: number;
	/** Running cumulative balance since the first month in the result set. */
	cumulativeBalance: number;
};

/** Aggregated amount for one category (or the uncategorized bucket). */
export type CategoryData = {
	/** Display name; {@code null} for uncategorized transactions. */
	categoryName: string | null;
	/**
	 * Hex colour from the category entity, e.g. {@code "#FF5733"}.
	 * {@code null} for uncategorized transactions — the frontend applies its own fallback colour.
	 */
	color: string | null;
	amount: number;
};

/** Full analytics payload returned by {@code GET /api/analytics}. */
export type AnalyticsResponse = {
	totalIncome: number;
	totalExpenses: number;
	balance: number;
	/** Savings rate in percent (0–100); {@code null} when there is no income. */
	savingsRate: number | null;
	monthlyData: MonthlyData[];
	categoryExpenses: CategoryData[];
};
