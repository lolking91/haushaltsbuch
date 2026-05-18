<script lang="ts">
	import Icon from '@iconify/svelte';
	import { _ } from 'svelte-i18n';
	import {
		Table,
		TableBody,
		TableCell,
		TableHead,
		TableHeader,
		TableRow
	} from '$lib/components/ui/table/index.js';
	import { Badge } from '$lib/components/ui/badge/index.js';
	import type { PageData } from './$types.js';

	let { data }: { data: PageData } = $props();

	// --- State ---

	let transactions = $state(data.transactions);
	let accounts = $state(data.accounts);
	let selectedAccountId = $state(data.selectedAccountId);
	let searchQuery = $state('');

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

		return result;
	});

	// --- Formatting helpers ---

	/** Formats a date string (YYYY-MM-DD) as a short locale date, e.g. "14.05.2026". */
	function formatDate(dateStr: string): string {
		return new Date(dateStr).toLocaleDateString('de-DE', {
			day: '2-digit',
			month: '2-digit',
			year: 'numeric'
		});
	}

	/** Formats a monetary amount in the given currency using German locale. */
	function formatAmount(amount: number, currency = 'EUR'): string {
		return new Intl.NumberFormat('de-DE', { style: 'currency', currency }).format(amount);
	}
</script>

<div class="space-y-4">
	<h1 class="text-2xl font-bold">{$_('transactions.title')}</h1>

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
			<Table>
				<TableHeader>
					<TableRow>
						<TableHead>{$_('transactions.col_date')}</TableHead>
						<TableHead>{$_('transactions.col_counterparty')}</TableHead>
						<TableHead class="hidden md:table-cell">{$_('transactions.col_purpose')}</TableHead>
						<TableHead class="hidden lg:table-cell">{$_('transactions.col_account')}</TableHead>
						<TableHead>{$_('transactions.col_type')}</TableHead>
						<TableHead class="text-right">{$_('transactions.col_amount')}</TableHead>
					</TableRow>
				</TableHeader>
				<TableBody>
					{#each filtered as tx (tx.id)}
						<TableRow>
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
							<TableCell
								class="text-right font-medium tabular-nums whitespace-nowrap
									   {tx.type === 'INCOME'
									? 'text-green-600 dark:text-green-400'
									: 'text-red-600 dark:text-red-400'}"
							>
								{formatAmount(tx.amount, tx.currency)}
							</TableCell>
						</TableRow>
					{/each}
				</TableBody>
			</Table>

			<!-- Row count footer -->
			<div
				class="px-4 py-3 border-t border-gray-200 dark:border-slate-700 text-xs text-gray-400 dark:text-slate-500"
			>
				{$_('transactions.row_count', { values: { count: filtered.length } })}
				{#if filtered.length !== transactions.length}
					{$_('transactions.row_count_filtered', { values: { total: transactions.length } })}
				{/if}
			</div>
		{/if}
	</div>
</div>
