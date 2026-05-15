<script lang="ts">
	import { base } from '$app/paths';
	import { onMount } from 'svelte';
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

			if (!accountsRes.ok || !txRes.ok) throw new Error('Fehler beim Laden der Daten');

			accounts = await accountsRes.json();
			transactions = await txRes.json();
		} catch (e) {
			errorMessage = e instanceof Error ? e.message : 'Unbekannter Fehler';
		} finally {
			loading = false;
		}
	});

	// --- Formatting helpers ---

	/** Formats a date string (YYYY-MM-DD) as a short German locale date, e.g. "14.05.2026". */
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
	<h1 class="text-2xl font-bold">Transaktionen</h1>

	<!-- Toolbar -->
	<div
		class="bg-white dark:bg-slate-800 rounded-xl border border-gray-200 dark:border-slate-700 shadow-sm p-4
			   flex flex-col sm:flex-row gap-3"
	>
		<!-- Search -->
		<div class="relative flex-1">
			<svg
				xmlns="http://www.w3.org/2000/svg"
				class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400 dark:text-slate-500 pointer-events-none"
				fill="none"
				viewBox="0 0 24 24"
				stroke="currentColor"
			>
				<path
					stroke-linecap="round"
					stroke-linejoin="round"
					stroke-width="2"
					d="M21 21l-4.35-4.35M17 11A6 6 0 1 1 5 11a6 6 0 0 1 12 0z"
				/>
			</svg>
			<input
				type="text"
				placeholder="Empfänger, Verwendungszweck…"
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
			<option value="">Alle Konten</option>
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
				<svg
					class="w-5 h-5 animate-spin text-blue-600"
					xmlns="http://www.w3.org/2000/svg"
					fill="none"
					viewBox="0 0 24 24"
				>
					<circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"
					></circle>
					<path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v8H4z"></path>
				</svg>
				<span class="text-sm">Lade Transaktionen…</span>
			</div>
		{:else if errorMessage}
			<div
				class="flex items-start gap-3 m-4 p-4 rounded-xl bg-red-50 dark:bg-red-950/30 border border-red-200 dark:border-red-800 text-red-700 dark:text-red-400"
			>
				<svg
					xmlns="http://www.w3.org/2000/svg"
					class="w-5 h-5 mt-0.5 shrink-0"
					fill="none"
					viewBox="0 0 24 24"
					stroke="currentColor"
				>
					<path
						stroke-linecap="round"
						stroke-linejoin="round"
						stroke-width="2"
						d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"
					/>
				</svg>
				<p class="text-sm">{errorMessage}</p>
			</div>
		{:else if filtered.length === 0}
			<div class="p-12 text-center text-gray-400 dark:text-slate-500 text-sm">
				{#if transactions.length === 0}
					Noch keine Transaktionen vorhanden. Importiere zuerst einen Kontoauszug.
				{:else}
					Keine Ergebnisse für die aktuelle Suche.
				{/if}
			</div>
		{:else}
			<Table>
				<TableHeader>
					<TableRow>
						<TableHead>Datum</TableHead>
						<TableHead>Empfänger / Auftraggeber</TableHead>
						<TableHead class="hidden md:table-cell">Verwendungszweck</TableHead>
						<TableHead class="hidden lg:table-cell">Konto</TableHead>
						<TableHead>Typ</TableHead>
						<TableHead class="text-right">Betrag</TableHead>
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
									{tx.type === 'INCOME' ? 'Einnahme' : 'Ausgabe'}
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

			<!-- Row count -->
			<div
				class="px-4 py-3 border-t border-gray-200 dark:border-slate-700 text-xs text-gray-400 dark:text-slate-500"
			>
				{filtered.length}
				{filtered.length === 1 ? 'Transaktion' : 'Transaktionen'}
				{#if filtered.length !== transactions.length}
					von {transactions.length} gesamt
				{/if}
			</div>
		{/if}
	</div>
</div>
