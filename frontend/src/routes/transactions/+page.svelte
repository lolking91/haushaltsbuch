<script lang="ts">
	import { base } from '$app/paths';
	import { onMount } from 'svelte';
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

	// --- Types ---

	type Account = {
		id: number;
		name: string;
		bankName: string;
		iban: string;
		currency: string;
		balance: number;
	};

	type Transaction = {
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

	// --- State ---

	let accounts: Account[] = $state([]);
	let transactions: Transaction[] = $state([]);
	let loading = $state(true);
	let errorMessage = $state('');

	let selectedAccountId: string = $state('');
	let searchQuery: string = $state('');

	// --- Derived ---

	let filtered: Transaction[] = $derived.by(() => {
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

	// --- Data loading ---

	onMount(async () => {
		try {
			const [accountsRes, txRes] = await Promise.all([
				fetch(`${base}/api/accounts`),
				fetch(`${base}/api/transactions`)
			]);

			if (!accountsRes.ok || !txRes.ok) throw new Error($_('transactions.error_load'));

			accounts = await accountsRes.json();
			transactions = await txRes.json();
		} catch (e) {
			errorMessage = e instanceof Error ? e.message : String(e);
		} finally {
			loading = false;
		}
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
		{#if loading}
			<div class="flex items-center justify-center gap-3 p-12 text-gray-400 dark:text-slate-500">
				<Icon icon="heroicons:arrow-path" class="w-5 h-5 animate-spin text-blue-600" />
				<span class="text-sm">{$_('transactions.loading')}</span>
			</div>
		{:else if errorMessage}
			<div
				class="flex items-start gap-3 m-4 p-4 rounded-xl bg-red-50 dark:bg-red-950/30 border border-red-200 dark:border-red-800 text-red-700 dark:text-red-400"
			>
				<Icon icon="heroicons:exclamation-circle" class="w-5 h-5 mt-0.5 shrink-0" />
				<p class="text-sm">{errorMessage}</p>
			</div>
		{:else if filtered.length === 0}
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
