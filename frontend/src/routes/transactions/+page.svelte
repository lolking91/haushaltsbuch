<script lang="ts">
	import Icon from '@iconify/svelte';
	import { _ } from 'svelte-i18n';
	import { goto } from '$app/navigation';
	import { base } from '$app/paths';
	import {
		Table,
		TableBody,
		TableCell,
		TableHead,
		TableHeader,
		TableRow
	} from '$lib/components/ui/table/index.js';
	import { Badge, CategoryBadge } from '$lib/components/ui/badge/index.js';
	import { Amount } from '$lib/components/ui/amount/index.js';
	import { DateInput } from '$lib/components/ui/date-input/index.js';
	import { NumberInput } from '$lib/components/ui/number-input/index.js';
	import { formatDate } from '$lib/utils/format.js';
	import type { PageData } from './$types.js';

	let { data }: { data: PageData } = $props();

	// --- Types ---

	type SortKey = 'bookingDate' | 'counterpartyName' | 'account' | 'type' | 'category' | 'amount';
	type SortDir = 'asc' | 'desc';
	type TypeFilter = 'ALL' | 'INCOME' | 'EXPENSE';

	// --- State ---

	let transactions = $state(data.transactions);
	let accounts = $state(data.accounts);
	let categories = $state(data.categories);

	// Quick search
	let searchQuery = $state('');

	// Advanced filter panel
	let showFilters = $state(false);
	let selectedAccountId = $state(data.selectedAccountId);
	let selectedType = $state<TypeFilter>('ALL');
	let selectedCategoryIds = $state(new Set<number>());
	let uncategorizedOnly = $state(false);
	let dateFrom = $state('');
	let dateTo = $state('');
	let amountMin = $state('');
	let amountMax = $state('');

	// Sorting & pagination
	let sortKey = $state<SortKey>('bookingDate');
	let sortDir = $state<SortDir>('desc');
	let currentPage = $state(0);
	let pageSize = $state(20);

	// --- Derived ---

	/** Number of active advanced filters (for the badge on the filter button). */
	let activeFilterCount = $derived(
		(selectedType !== 'ALL' ? 1 : 0) +
		(selectedAccountId ? 1 : 0) +
		(selectedCategoryIds.size > 0 ? 1 : 0) +
		(uncategorizedOnly ? 1 : 0) +
		(dateFrom ? 1 : 0) +
		(dateTo ? 1 : 0) +
		(amountMin ? 1 : 0) +
		(amountMax ? 1 : 0)
	);

	let filtered = $derived.by(() => {
		let result = transactions;

		if (selectedAccountId) {
			result = result.filter((t) => t.account.id === Number(selectedAccountId));
		}

		if (selectedType !== 'ALL') {
			result = result.filter((t) => t.type === selectedType);
		}

		if (uncategorizedOnly) {
			result = result.filter((t) => t.category === null);
		} else if (selectedCategoryIds.size > 0) {
			result = result.filter((t) => t.category !== null && selectedCategoryIds.has(t.category!.id));
		}

		if (dateFrom) {
			result = result.filter((t) => t.bookingDate >= dateFrom);
		}

		if (dateTo) {
			result = result.filter((t) => t.bookingDate <= dateTo);
		}

		if (amountMin) {
			result = result.filter((t) => Math.abs(t.amount) >= Number(amountMin));
		}

		if (amountMax) {
			result = result.filter((t) => Math.abs(t.amount) <= Number(amountMax));
		}

		const q = searchQuery.trim().toLowerCase();
		if (q) {
			result = result.filter(
				(t) =>
					t.counterpartyName?.toLowerCase().includes(q) ||
					t.description?.toLowerCase().includes(q) ||
					t.bookingText?.toLowerCase().includes(q)
			);
		}

		const sorted = [...result];
		sorted.sort((a, b) => {
			let cmp = 0;
			switch (sortKey) {
				case 'bookingDate':
					cmp = a.bookingDate.localeCompare(b.bookingDate);
					break;
				case 'counterpartyName':
					cmp = (a.counterpartyName ?? '').localeCompare(b.counterpartyName ?? '');
					break;
				case 'account':
					cmp = a.account.name.localeCompare(b.account.name);
					break;
				case 'type':
					cmp = a.type.localeCompare(b.type);
					break;
				case 'category':
					cmp = (a.category?.name ?? '').localeCompare(b.category?.name ?? '');
					break;
				case 'amount':
					cmp = a.amount - b.amount;
					break;
			}
			return sortDir === 'asc' ? cmp : -cmp;
		});
		return sorted;
	});

	let totalPages = $derived(Math.ceil(filtered.length / pageSize));
	let paginated = $derived(filtered.slice(currentPage * pageSize, (currentPage + 1) * pageSize));

	// Reset to page 0 whenever any filter or page size changes.
	$effect(() => {
		searchQuery;
		selectedAccountId;
		selectedType;
		selectedCategoryIds.size;
		uncategorizedOnly;
		dateFrom;
		dateTo;
		amountMin;
		amountMax;
		pageSize;
		currentPage = 0;
	});

	// When uncategorizedOnly is activated, clear any selected category chips.
	$effect(() => {
		if (uncategorizedOnly) {
			selectedCategoryIds = new Set();
		}
	});

	// --- Actions ---

	/** Toggles sort direction when the same column is clicked; sets new key with ascending order otherwise. */
	function toggleSort(key: SortKey) {
		if (sortKey === key) {
			sortDir = sortDir === 'asc' ? 'desc' : 'asc';
		} else {
			sortKey = key;
			sortDir = 'asc';
		}
		currentPage = 0;
	}

	/** Toggles a category ID in the multi-select set. */
	function toggleCategory(id: number) {
		const next = new Set(selectedCategoryIds);
		if (next.has(id)) {
			next.delete(id);
		} else {
			uncategorizedOnly = false;
			next.add(id);
		}
		// Reassign instead of mutating so Svelte 5 picks up the change.
		selectedCategoryIds = next;
	}

	/** Resets all advanced filters to their default values. */
	function resetFilters() {
		selectedAccountId = '';
		selectedType = 'ALL';
		selectedCategoryIds = new Set();
		uncategorizedOnly = false;
		dateFrom = '';
		dateTo = '';
		amountMin = '';
		amountMax = '';
	}

</script>


<div class="space-y-4">
	<h1 class="text-2xl font-bold">{$_('nav.transactions')}</h1>

	<!-- Toolbar -->
	<div
		class="bg-white dark:bg-slate-800 rounded-xl border border-gray-200 dark:border-slate-700 shadow-sm p-4 space-y-3"
	>
		<!-- Quick search row -->
		<div class="flex flex-col sm:flex-row gap-3">
			<!-- Search -->
			<div class="relative flex-1">
				<Icon
					icon="heroicons:magnifying-glass"
					class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400 dark:text-slate-500 pointer-events-none"
				/>
				<input
					type="text"
					placeholder={$_('transactions.search_placeholder')}
					bind:value={searchQuery}
					class="w-full pl-9 pr-3 py-2 rounded-lg border border-gray-300 dark:border-slate-600
						   bg-white dark:bg-slate-900 text-sm outline-none
						   focus:ring-2 focus:ring-blue-500"
				/>
			</div>

			<!-- Filter toggle button -->
			<button
				onclick={() => (showFilters = !showFilters)}
				class="flex items-center gap-2 px-3 py-2 rounded-lg border text-sm font-medium transition-colors
					   {activeFilterCount > 0
						? 'border-blue-500 bg-blue-50 text-blue-700 dark:bg-blue-900/30 dark:text-blue-300 dark:border-blue-500'
						: 'border-gray-300 dark:border-slate-600 bg-white dark:bg-slate-900 text-gray-700 dark:text-slate-300 hover:bg-gray-50 dark:hover:bg-slate-800'}"
			>
				<Icon icon="heroicons:adjustments-horizontal" class="w-4 h-4 shrink-0" />
				{$_('transactions.filter_btn')}
				{#if activeFilterCount > 0}
					<span
						class="inline-flex items-center justify-center w-5 h-5 rounded-full text-xs font-bold
							   bg-blue-600 text-white dark:bg-blue-400 dark:text-slate-900"
					>
						{activeFilterCount}
					</span>
				{/if}
				<Icon
					icon={showFilters ? 'heroicons:chevron-up' : 'heroicons:chevron-down'}
					class="w-3.5 h-3.5 shrink-0 opacity-60"
				/>
			</button>
		</div>

		<!-- Advanced filter panel (collapsible) -->
		{#if showFilters}
			<div
				class="pt-3 border-t border-gray-200 dark:border-slate-700 space-y-4"
			>
				<!-- Row 1: Type, Account, Date range -->
				<div class="flex flex-wrap gap-4 items-end">
					<!-- Type toggle -->
					<div class="space-y-1">
						<label class="text-xs font-medium text-gray-500 dark:text-slate-400 uppercase tracking-wide">
							{$_('transactions.filter_type_label')}
						</label>
						<div class="flex rounded-lg border border-gray-300 dark:border-slate-600 overflow-hidden text-sm">
							{#each [
								{ value: 'ALL', label: $_('transactions.filter_type_all') },
								{ value: 'INCOME', label: $_('transactions.badge_income') },
								{ value: 'EXPENSE', label: $_('transactions.badge_expense') }
							] as opt (opt.value)}
								<button
									onclick={() => (selectedType = opt.value as TypeFilter)}
									class="px-3 py-1.5 transition-colors
										   {selectedType === opt.value
											? 'bg-blue-600 text-white font-medium'
											: 'bg-white dark:bg-slate-900 text-gray-600 dark:text-slate-300 hover:bg-gray-50 dark:hover:bg-slate-800'}"
								>
									{opt.label}
								</button>
							{/each}
						</div>
					</div>

					<!-- Account -->
					<div class="space-y-1">
						<label class="text-xs font-medium text-gray-500 dark:text-slate-400 uppercase tracking-wide">
							{$_('transactions.filter_account_label')}
						</label>
						<select
							bind:value={selectedAccountId}
							class="px-3 py-2 rounded-lg border border-gray-300 dark:border-slate-600
								   bg-white dark:bg-slate-900 text-sm outline-none
								   focus:ring-2 focus:ring-blue-500"
						>
							<option value="">{$_('transactions.filter_all_accounts')}</option>
							{#each accounts as account (account.id)}
								<option value={account.id}>{account.name}</option>
							{/each}
						</select>
					</div>

					<!-- Date range -->
					<div class="space-y-1">
						<label class="text-xs font-medium text-gray-500 dark:text-slate-400 uppercase tracking-wide">
							{$_('transactions.filter_date_from')} / {$_('transactions.filter_date_to')}
						</label>
						<div class="flex items-center gap-2">
							<DateInput bind:value={dateFrom} max={dateTo || undefined} />
							<span class="text-gray-400 dark:text-slate-500 text-sm">–</span>
							<DateInput bind:value={dateTo} min={dateFrom || undefined} />
						</div>
					</div>

					<!-- Amount range -->
					<div class="space-y-1">
						<label class="text-xs font-medium text-gray-500 dark:text-slate-400 uppercase tracking-wide">
							{$_('transactions.filter_amount_min')} / {$_('transactions.filter_amount_max')}
						</label>
						<div class="flex items-center gap-2">
							<NumberInput bind:value={amountMin} min={0} step={0.01} placeholder="0,00" class="w-24" />
							<span class="text-gray-400 dark:text-slate-500 text-sm">–</span>
							<NumberInput bind:value={amountMax} min={0} step={0.01} placeholder="∞" class="w-24" />
						</div>
					</div>
				</div>

				<!-- Row 2: Categories multi-select -->
				{#if categories.length > 0}
					<div class="space-y-1.5">
						<label class="text-xs font-medium text-gray-500 dark:text-slate-400 uppercase tracking-wide">
							{$_('transactions.filter_category_label')}
						</label>
						<div class="flex flex-wrap gap-2">
							<!-- Uncategorized chip -->
							<button
								onclick={() => {
									uncategorizedOnly = !uncategorizedOnly;
									if (uncategorizedOnly) selectedCategoryIds = new Set();
								}}
								class="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-medium
									   border transition-colors
									   {uncategorizedOnly
										? 'bg-gray-700 text-white border-gray-700 dark:bg-gray-300 dark:text-gray-900 dark:border-gray-300'
										: 'border-gray-300 dark:border-slate-600 text-gray-600 dark:text-slate-300 hover:border-gray-400 dark:hover:border-slate-500'}"
							>
								<span class="w-2 h-2 rounded-full bg-gray-400 dark:bg-slate-500 shrink-0"></span>
								{$_('transactions.filter_uncategorized')}
							</button>

							{#each categories as cat (cat.id)}
								<button
									onclick={() => toggleCategory(cat.id)}
									class="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-medium
										   border transition-colors
										   {selectedCategoryIds.has(cat.id)
											? 'text-white border-transparent'
											: 'border-gray-300 dark:border-slate-600 text-gray-600 dark:text-slate-300 hover:border-gray-400 dark:hover:border-slate-500'}"
									style={selectedCategoryIds.has(cat.id) ? `background-color: ${cat.color}; border-color: ${cat.color}` : ''}
								>
									<span
										class="w-2 h-2 rounded-full shrink-0"
										style="background-color: {cat.color}"
									></span>
									{cat.name}
								</button>
							{/each}
						</div>
					</div>
				{/if}

				<!-- Row 3: Reset button -->
				{#if activeFilterCount > 0}
					<div class="flex justify-end">
						<button
							onclick={resetFilters}
							class="flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-sm text-gray-500 dark:text-slate-400
								   hover:text-gray-700 dark:hover:text-slate-200 hover:bg-gray-100 dark:hover:bg-slate-700 transition-colors"
						>
							<Icon icon="heroicons:x-mark" class="w-3.5 h-3.5" />
							{$_('transactions.filter_reset')}
						</button>
					</div>
				{/if}
			</div>
		{/if}
	</div>

	<!-- Table card -->
	<div
		class="bg-white dark:bg-slate-800 rounded-xl border border-gray-200 dark:border-slate-700 shadow-sm overflow-hidden"
	>
		{#if filtered.length === 0}
			<div class="p-12 text-center text-gray-400 dark:text-slate-500 text-sm">
				{#if transactions.length === 0}
					{$_('transactions.empty_no_import')}
				{:else}
					{$_('transactions.empty_no_results')}
				{/if}
			</div>
		{:else}
		{#snippet sortIcon(key: SortKey)}
			{#if sortKey === key}
				<Icon
					icon={sortDir === 'asc' ? 'heroicons:chevron-up' : 'heroicons:chevron-down'}
					class="w-3.5 h-3.5 shrink-0"
				/>
			{:else}
				<Icon icon="heroicons:chevron-up-down" class="w-3.5 h-3.5 shrink-0 opacity-40" />
			{/if}
		{/snippet}

			<Table>
				<TableHeader>
					<TableRow>
						<TableHead
							class="cursor-pointer select-none hover:text-gray-900 dark:hover:text-slate-100"
							onclick={() => toggleSort('bookingDate')}
						>
							<div class="flex items-center gap-1">
								{$_('transactions.col_date')}
								{@render sortIcon('bookingDate')}
							</div>
						</TableHead>
						<TableHead
							class="cursor-pointer select-none hover:text-gray-900 dark:hover:text-slate-100"
							onclick={() => toggleSort('counterpartyName')}
						>
							<div class="flex items-center gap-1">
								{$_('transactions.col_counterparty')}
								{@render sortIcon('counterpartyName')}
							</div>
						</TableHead>
						<TableHead class="hidden md:table-cell">{$_('transactions.col_purpose')}</TableHead>
						<TableHead
							class="hidden lg:table-cell cursor-pointer select-none hover:text-gray-900 dark:hover:text-slate-100"
							onclick={() => toggleSort('account')}
						>
							<div class="flex items-center gap-1">
								{$_('transactions.col_account')}
								{@render sortIcon('account')}
							</div>
						</TableHead>
						<TableHead
							class="cursor-pointer select-none hover:text-gray-900 dark:hover:text-slate-100"
							onclick={() => toggleSort('type')}
						>
							<div class="flex items-center gap-1">
								{$_('transactions.col_type')}
								{@render sortIcon('type')}
							</div>
						</TableHead>
						<TableHead
							class="cursor-pointer select-none hover:text-gray-900 dark:hover:text-slate-100"
							onclick={() => toggleSort('category')}
						>
							<div class="flex items-center gap-1">
								{$_('transactions.col_category')}
								{@render sortIcon('category')}
							</div>
						</TableHead>
						<TableHead
							class="text-right cursor-pointer select-none hover:text-gray-900 dark:hover:text-slate-100"
							onclick={() => toggleSort('amount')}
						>
							<div class="flex items-center justify-end gap-1">
								{$_('transactions.col_amount')}
								{@render sortIcon('amount')}
							</div>
						</TableHead>
					</TableRow>
				</TableHeader>
				<TableBody>
					{#each paginated as tx (tx.id)}
						<TableRow
							class="cursor-pointer"
							onclick={() => goto(`${base}/transactions/${tx.id}`)}
						>
							<TableCell class="whitespace-nowrap text-gray-500 dark:text-slate-400">
								{formatDate(tx.bookingDate)}
							</TableCell>
							<TableCell class="font-medium">
								{tx.counterpartyName ?? '—'}
								{#if tx.bookingText}
									<p class="text-xs text-gray-400 dark:text-slate-500 font-normal mt-0.5">
										{tx.bookingText}
									</p>
								{/if}
							</TableCell>
							<TableCell class="hidden md:table-cell text-sm text-gray-600 dark:text-slate-300 max-w-xs truncate">
								{tx.description ?? '—'}
							</TableCell>
							<TableCell class="hidden lg:table-cell text-sm text-gray-500 dark:text-slate-400">
								{tx.account.name}
							</TableCell>
							<TableCell>
								<Badge variant={tx.type === 'INCOME' ? 'income' : 'expense'}>
									{tx.type === 'INCOME'
										? $_('transactions.badge_income')
										: $_('transactions.badge_expense')}
								</Badge>
							</TableCell>
							<TableCell>
								<CategoryBadge category={tx.category} />
							</TableCell>
							<TableCell class="text-right">
								<Amount amount={tx.amount} currency={tx.currency} type={tx.type} class="font-medium" />
							</TableCell>
						</TableRow>
					{/each}
				</TableBody>
			</Table>

			<!-- Pagination footer -->
			<div
				class="px-4 py-3 border-t border-gray-200 dark:border-slate-700
					   flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3
					   text-xs text-gray-500 dark:text-slate-400"
			>
				<!-- Left: page size selector + row count -->
				<div class="flex items-center gap-4">
					<div class="flex items-center gap-2">
						<span>{$_('transactions.page_size_label')}</span>
						<select
							bind:value={pageSize}
							class="px-2 py-1 rounded border border-gray-300 dark:border-slate-600
								   bg-white dark:bg-slate-900 outline-none focus:ring-2 focus:ring-blue-500"
						>
							{#each [10, 20, 50, 100] as size}
								<option value={size}>{size}</option>
							{/each}
						</select>
					</div>
					<span>
						{$_('transactions.row_count', { values: { count: filtered.length } })}
						{#if filtered.length !== transactions.length}
							{$_('transactions.row_count_filtered', { values: { total: transactions.length } })}
						{/if}
					</span>
				</div>

				<!-- Right: prev / page indicator / next -->
				<div class="flex items-center gap-2">
					<button
						onclick={() => (currentPage -= 1)}
						disabled={currentPage === 0}
						class="p-1 rounded hover:bg-gray-100 dark:hover:bg-slate-700 disabled:opacity-30 disabled:cursor-not-allowed transition-colors"
						aria-label="Vorherige Seite"
					>
						<Icon icon="heroicons:chevron-left" class="w-4 h-4" />
					</button>
					<span class="tabular-nums">
						{$_('transactions.page_indicator', { values: { current: currentPage + 1, total: totalPages } })}
					</span>
					<button
						onclick={() => (currentPage += 1)}
						disabled={currentPage >= totalPages - 1}
						class="p-1 rounded hover:bg-gray-100 dark:hover:bg-slate-700 disabled:opacity-30 disabled:cursor-not-allowed transition-colors"
						aria-label="Nächste Seite"
					>
						<Icon icon="heroicons:chevron-right" class="w-4 h-4" />
					</button>
				</div>
			</div>
		{/if}
	</div>
</div>
