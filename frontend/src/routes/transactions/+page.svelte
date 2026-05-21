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
	import { formatDate } from '$lib/utils/format.js';
	import type { PageData } from './$types.js';

	let { data }: { data: PageData } = $props();

	// --- Types ---

	type SortKey = 'bookingDate' | 'counterpartyName' | 'account' | 'type' | 'category' | 'amount';
	type SortDir = 'asc' | 'desc';

	// --- State ---

	let transactions = $state(data.transactions);
	let accounts = $state(data.accounts);
	let selectedAccountId = $state(data.selectedAccountId);
	let searchQuery = $state('');
	let sortKey = $state<SortKey>('bookingDate');
	let sortDir = $state<SortDir>('desc');
	let currentPage = $state(0);
	let pageSize = $state(20);

	// --- Derived ---

	let filtered = $derived.by(() => {
		let result = transactions;

		if (selectedAccountId) {
			result = result.filter((t) => t.account.id === Number(selectedAccountId));
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

	// Reset to page 0 whenever filter inputs or page size change.
	$effect(() => {
		searchQuery;
		selectedAccountId;
		pageSize;
		currentPage = 0;
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

</script>

<div class="space-y-4">
	<h1 class="text-2xl font-bold">{$_('nav.transactions')}</h1>

	<!-- Toolbar -->
	<div
		class="bg-white dark:bg-slate-800 rounded-xl border border-gray-200 dark:border-slate-700 shadow-sm p-4
			   flex flex-col sm:flex-row gap-3"
	>
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

		<!-- Account filter -->
		<select
			bind:value={selectedAccountId}
			class="px-3 py-2 rounded-lg border border-gray-300 dark:border-slate-600
				   bg-white dark:bg-slate-900 text-sm outline-none
				   focus:ring-2 focus:ring-blue-500"
		>
			<option value="">{$_('transactions.all_accounts')}</option>
			{#each accounts as account}
				<option value={account.id}>{account.name}</option>
			{/each}
		</select>
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
