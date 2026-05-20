<script lang="ts">
	import { onMount } from 'svelte';
	import { base } from '$app/paths';
	import Icon from '@iconify/svelte';
	import { _ } from 'svelte-i18n';
	import { Badge } from '$lib/components/ui/badge/index.js';
	import { Amount } from '$lib/components/ui/amount/index.js';
	import { formatCurrency, formatDateShort, formatIban } from '$lib/utils/format.js';
	import type { Account, Transaction } from '$lib/types/types.js';
	import { accountsApi } from '$lib/api/accounts.js';
	import { transactionsApi } from '$lib/api/transactions.js';

	// --- State ---

	let accounts: Account[] = $state([]);
	let transactions: Transaction[] = $state([]);
	let loading = $state(true);
	let errorMessage = $state('');

	// --- Derived ---

	/** Sum of all account balances (assumes a single currency for display). */
	let totalBalance = $derived(accounts.reduce((sum, a) => sum + a.balance, 0));

	/** Transactions booked in the current calendar month. */
	let thisMonthTx: Transaction[] = $derived.by(() => {
		const now = new Date();
		return transactions.filter((t) => {
			const d = new Date(t.bookingDate);
			return d.getFullYear() === now.getFullYear() && d.getMonth() === now.getMonth();
		});
	});

	let incomeThisMonth = $derived(
		thisMonthTx.filter((t) => t.type === 'INCOME').reduce((sum, t) => sum + t.amount, 0)
	);

	/** Shown as a positive number even though amounts are stored negative. */
	let expensesThisMonth = $derived(
		thisMonthTx.filter((t) => t.type === 'EXPENSE').reduce((sum, t) => sum + Math.abs(t.amount), 0)
	);

	/** Five most recent transactions sorted by booking date descending. */
	let recentTransactions: Transaction[] = $derived(
		[...transactions]
			.sort((a, b) => new Date(b.bookingDate).getTime() - new Date(a.bookingDate).getTime())
			.slice(0, 5)
	);

	// --- Data loading ---

	onMount(async () => {
		try {
			[accounts, transactions] = await Promise.all([
				accountsApi.getAll(),
				transactionsApi.getAll()
			]);
		} catch (e) {
			errorMessage = e instanceof Error ? e.message : String(e);
		} finally {
			loading = false;
		}
	});

</script>

<h1 class="text-2xl font-bold mb-6">{$_('nav.dashboard')}</h1>

<!-- Loading -->
{#if loading}
	<div class="flex items-center justify-center gap-3 py-24 text-gray-400 dark:text-slate-500">
		<Icon icon="heroicons:arrow-path" class="w-6 h-6 animate-spin text-blue-600" />
		<span class="text-sm">Lade…</span>
	</div>

<!-- Error -->
{:else if errorMessage}
	<div class="flex items-start gap-3 p-4 rounded-xl bg-red-50 dark:bg-red-950/30 border border-red-200 dark:border-red-800 text-red-700 dark:text-red-400">
		<Icon icon="heroicons:exclamation-circle" class="w-5 h-5 mt-0.5 shrink-0" />
		<p class="text-sm">{errorMessage}</p>
	</div>

<!-- Empty state: no accounts yet -->
{:else if accounts.length === 0}
	<div class="flex flex-col items-center justify-center gap-4 py-24 text-center">
		<div class="w-16 h-16 rounded-full bg-blue-50 dark:bg-blue-950/40 flex items-center justify-center">
			<Icon icon="heroicons:inbox" class="w-8 h-8 text-blue-500 dark:text-blue-400" />
		</div>
		<div>
			<p class="font-semibold text-gray-700 dark:text-slate-300">{$_('dashboard.no_data_title')}</p>
			<p class="text-sm text-gray-400 dark:text-slate-500 mt-1">{$_('dashboard.no_data_text')}</p>
		</div>
		<a
			href="{base}/import"
			class="inline-flex items-center gap-2 px-4 py-2 rounded-xl bg-blue-600 hover:bg-blue-700 text-white text-sm font-medium transition-colors"
		>
			<Icon icon="heroicons:arrow-up-tray" class="w-4 h-4" />
			{$_('dashboard.go_to_import')}
		</a>
	</div>

<!-- Dashboard with data -->
{:else}
	<!-- ── Stats row ──────────────────────────────────────────────────────── -->
	<div class="grid grid-cols-1 sm:grid-cols-3 gap-4 mb-6">

		<!-- Total balance -->
		<div class="bg-white dark:bg-slate-800 rounded-xl border border-gray-200 dark:border-slate-700 p-5 shadow-sm flex items-center gap-4">
			<div class="w-11 h-11 rounded-lg bg-blue-50 dark:bg-blue-950/40 flex items-center justify-center shrink-0">
				<Icon icon="heroicons:banknotes" class="w-6 h-6 text-blue-600 dark:text-blue-400" />
			</div>
			<div class="min-w-0">
				<p class="text-xs text-gray-500 dark:text-slate-400 uppercase tracking-wide">{$_('dashboard.total_balance')}</p>
				<p class="text-2xl font-bold tabular-nums truncate mt-0.5">{formatCurrency(totalBalance)}</p>
			</div>
		</div>

		<!-- Income this month -->
		<div class="bg-white dark:bg-slate-800 rounded-xl border border-gray-200 dark:border-slate-700 p-5 shadow-sm flex items-center gap-4">
			<div class="w-11 h-11 rounded-lg bg-green-50 dark:bg-green-950/40 flex items-center justify-center shrink-0">
				<Icon icon="heroicons:arrow-trending-up" class="w-6 h-6 text-green-600 dark:text-green-400" />
			</div>
			<div class="min-w-0">
				<p class="text-xs text-gray-500 dark:text-slate-400 uppercase tracking-wide">{$_('dashboard.income_month')}</p>
				<p class="text-2xl font-bold tabular-nums text-green-600 dark:text-green-400 truncate mt-0.5">
					{formatCurrency(incomeThisMonth)}
				</p>
			</div>
		</div>

		<!-- Expenses this month -->
		<div class="bg-white dark:bg-slate-800 rounded-xl border border-gray-200 dark:border-slate-700 p-5 shadow-sm flex items-center gap-4">
			<div class="w-11 h-11 rounded-lg bg-red-50 dark:bg-red-950/40 flex items-center justify-center shrink-0">
				<Icon icon="heroicons:arrow-trending-down" class="w-6 h-6 text-red-600 dark:text-red-400" />
			</div>
			<div class="min-w-0">
				<p class="text-xs text-gray-500 dark:text-slate-400 uppercase tracking-wide">{$_('dashboard.expenses_month')}</p>
				<p class="text-2xl font-bold tabular-nums text-red-600 dark:text-red-400 truncate mt-0.5">
					{formatCurrency(expensesThisMonth)}
				</p>
			</div>
		</div>
	</div>

	<!-- ── Accounts + Recent transactions ────────────────────────────────── -->
	<div class="grid grid-cols-1 lg:grid-cols-2 gap-6">

		<!-- Accounts -->
		<div class="space-y-3">
			<h2 class="font-semibold text-sm text-gray-500 dark:text-slate-400 uppercase tracking-wide">
				{$_('dashboard.my_accounts')}
			</h2>
			{#each accounts as account (account.id)}
				<div class="bg-white dark:bg-slate-800 rounded-xl border border-gray-200 dark:border-slate-700 p-5 shadow-sm">
					<div class="flex items-start justify-between gap-4">
						<div class="flex items-center gap-3 min-w-0">
							<div class="w-10 h-10 rounded-lg bg-slate-100 dark:bg-slate-700 flex items-center justify-center shrink-0">
								<Icon icon="heroicons:building-library" class="w-5 h-5 text-slate-500 dark:text-slate-400" />
							</div>
							<div class="min-w-0">
								<p class="font-semibold truncate">{account.name}</p>
								<p class="text-xs text-gray-400 dark:text-slate-500 truncate">{account.bankName}</p>
							</div>
						</div>
						<p class="text-lg font-bold tabular-nums whitespace-nowrap
							{account.balance >= 0 ? 'text-gray-900 dark:text-slate-100' : 'text-red-600 dark:text-red-400'}">
							{formatCurrency(account.balance, account.currency)}
						</p>
					</div>
					<!-- font-mono: monospaced for IBAN readability -->
					<p class="text-xs font-mono text-gray-400 dark:text-slate-500 tracking-wider mt-3 pt-3 border-t border-gray-100 dark:border-slate-700">
						{formatIban(account.iban)}
					</p>
				</div>
			{/each}
		</div>

		<!-- Recent transactions -->
		<div class="space-y-3">
			<div class="flex items-center justify-between">
				<h2 class="font-semibold text-sm text-gray-500 dark:text-slate-400 uppercase tracking-wide">
					{$_('dashboard.recent_transactions')}
				</h2>
				{#if transactions.length > 0}
					<a
						href="{base}/transactions"
						class="text-xs text-blue-600 dark:text-blue-400 hover:underline font-medium"
					>
						{$_('dashboard.show_all')} →
					</a>
				{/if}
			</div>

			<div class="bg-white dark:bg-slate-800 rounded-xl border border-gray-200 dark:border-slate-700 shadow-sm overflow-hidden">
				{#if recentTransactions.length === 0}
					<div class="p-8 text-center text-sm text-gray-400 dark:text-slate-500">
						{$_('dashboard.no_recent')}
					</div>
				{:else}
					<ul class="divide-y divide-gray-100 dark:divide-slate-700">
						{#each recentTransactions as tx (tx.id)}
							<li class="flex items-center gap-3 px-4 py-3 hover:bg-gray-50 dark:hover:bg-slate-700/50 transition-colors">

								<!-- Type indicator dot -->
								<span class="w-2 h-2 rounded-full shrink-0
									{tx.type === 'INCOME' ? 'bg-green-500' : 'bg-red-500'}">
								</span>

								<!-- Date -->
								<span class="text-xs text-gray-400 dark:text-slate-500 whitespace-nowrap w-14 shrink-0">
									{formatDateShort(tx.bookingDate)}
								</span>

								<!-- Counterparty -->
								<span class="text-sm flex-1 truncate">
									{tx.counterpartyName ?? '—'}
								</span>

								<!-- Badge + amount -->
								<div class="flex items-center gap-2 shrink-0">
									<Badge variant={tx.type === 'INCOME' ? 'income' : 'expense'} class="hidden sm:inline-flex">
										{tx.type === 'INCOME' ? $_('transactions.badge_income') : $_('transactions.badge_expense')}
									</Badge>
									<Amount amount={tx.amount} currency={tx.currency} type={tx.type} class="text-sm font-semibold" />
								</div>
							</li>
						{/each}
					</ul>
				{/if}
			</div>
		</div>
	</div>
{/if}
