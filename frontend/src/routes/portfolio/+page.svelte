<script lang="ts">
	import { onMount } from 'svelte';
	import { base } from '$app/paths';
	import Icon from '@iconify/svelte';
	import { _ } from 'svelte-i18n';
	import { formatCurrency, formatDate } from '$lib/utils/format.js';
	import type { Etf } from '$lib/types/types.js';
	import { etfsApi } from '$lib/api/etfs.js';

	// --- State ---

	let etfs: Etf[] = $state([]);
	let loading = $state(true);
	let errorMessage = $state('');
	let confirmDeleteId: number | null = $state(null);
	let deleteError = $state('');

	// --- Derived ---

	/** Sum of the latest snapshot values across all ETFs. */
	let totalValue = $derived(
		etfs.reduce((sum, e) => sum + (e.latestSnapshot?.totalValue ?? 0), 0)
	);

	/** Sum of the latest cumulative gains across all ETFs. */
	let totalGain = $derived(
		etfs.reduce((sum, e) => sum + (e.latestSnapshot?.gainAbsolute ?? 0), 0)
	);

	// --- Data loading ---

	onMount(async () => {
		try {
			etfs = await etfsApi.getAll();
		} catch (e) {
			errorMessage = e instanceof Error ? e.message : String(e);
		} finally {
			loading = false;
		}
	});

	// --- Actions ---

	async function deleteEtf(id: number) {
		deleteError = '';
		try {
			await etfsApi.delete(id);
			etfs = etfs.filter((e) => e.id !== id);
			confirmDeleteId = null;
		} catch (e: unknown) {
			const msg = e instanceof Error ? e.message : String(e);
			// 409 means the ETF still has snapshots
			deleteError = msg.includes('409') || msg.toLowerCase().includes('snapshot')
				? $_('portfolio.delete_has_snapshots')
				: $_('common.delete_error');
		}
	}
</script>

<div class="flex items-center justify-between mb-6">
	<h1 class="text-2xl font-bold">{$_('portfolio.title')}</h1>
	<div class="flex gap-2">
		<a
			href="{base}/import?tab=etf"
			class="inline-flex items-center gap-2 px-3 py-2 rounded-lg text-sm font-medium
			       border border-gray-300 dark:border-slate-600
			       hover:bg-gray-100 dark:hover:bg-slate-700 transition-colors"
		>
			<Icon icon="heroicons:arrow-up-tray" class="w-4 h-4" />
			{$_('portfolio.btn_import')}
		</a>
		<a
			href="{base}/portfolio/new"
			class="inline-flex items-center gap-2 px-3 py-2 rounded-lg text-sm font-medium
			       bg-blue-600 hover:bg-blue-700 text-white transition-colors"
		>
			<Icon icon="heroicons:plus" class="w-4 h-4" />
			{$_('portfolio.btn_new')}
		</a>
	</div>
</div>

<!-- Loading -->
{#if loading}
	<div class="flex items-center justify-center gap-3 py-24 text-gray-400 dark:text-slate-500">
		<Icon icon="heroicons:arrow-path" class="w-6 h-6 animate-spin text-blue-600" />
		<span class="text-sm">{$_('portfolio.loading')}</span>
	</div>

<!-- Error -->
{:else if errorMessage}
	<div class="flex items-start gap-3 p-4 rounded-xl bg-red-50 dark:bg-red-950/30 border border-red-200 dark:border-red-800 text-red-700 dark:text-red-400">
		<Icon icon="heroicons:exclamation-circle" class="w-5 h-5 mt-0.5 shrink-0" />
		<p class="text-sm">{errorMessage}</p>
	</div>

<!-- Empty state -->
{:else if etfs.length === 0}
	<div class="flex flex-col items-center justify-center gap-4 py-24 text-center">
		<div class="w-16 h-16 rounded-full bg-blue-50 dark:bg-blue-950/40 flex items-center justify-center">
			<Icon icon="heroicons:chart-bar" class="w-8 h-8 text-blue-500 dark:text-blue-400" />
		</div>
		<div>
			<p class="font-semibold text-gray-700 dark:text-slate-300">{$_('portfolio.empty_title')}</p>
			<p class="text-sm text-gray-400 dark:text-slate-500 mt-1">{$_('portfolio.empty_text')}</p>
		</div>
		<a
			href="{base}/import?tab=etf"
			class="inline-flex items-center gap-2 px-4 py-2 rounded-xl bg-blue-600 hover:bg-blue-700 text-white text-sm font-medium transition-colors"
		>
			<Icon icon="heroicons:arrow-up-tray" class="w-4 h-4" />
			{$_('portfolio.empty_cta')}
		</a>
	</div>

<!-- Data -->
{:else}
	<!-- ── Stats row ──────────────────────────────────────────────────────── -->
	<div class="grid grid-cols-1 sm:grid-cols-3 gap-4 mb-6">

		<!-- Total value -->
		<div class="bg-white dark:bg-slate-800 rounded-xl border border-gray-200 dark:border-slate-700 p-5 shadow-sm flex items-center gap-4">
			<div class="w-11 h-11 rounded-lg bg-blue-50 dark:bg-blue-950/40 flex items-center justify-center shrink-0">
				<Icon icon="heroicons:chart-bar" class="w-6 h-6 text-blue-600 dark:text-blue-400" />
			</div>
			<div class="min-w-0">
				<p class="text-xs text-gray-500 dark:text-slate-400 uppercase tracking-wide">{$_('portfolio.total_value')}</p>
				<p class="text-2xl font-bold tabular-nums truncate mt-0.5">{formatCurrency(totalValue)}</p>
			</div>
		</div>

		<!-- Total gain -->
		<div class="bg-white dark:bg-slate-800 rounded-xl border border-gray-200 dark:border-slate-700 p-5 shadow-sm flex items-center gap-4">
			<div class="w-11 h-11 rounded-lg {totalGain >= 0 ? 'bg-green-50 dark:bg-green-950/40' : 'bg-red-50 dark:bg-red-950/40'} flex items-center justify-center shrink-0">
				<Icon
					icon={totalGain >= 0 ? 'heroicons:arrow-trending-up' : 'heroicons:arrow-trending-down'}
					class="w-6 h-6 {totalGain >= 0 ? 'text-green-600 dark:text-green-400' : 'text-red-600 dark:text-red-400'}"
				/>
			</div>
			<div class="min-w-0">
				<p class="text-xs text-gray-500 dark:text-slate-400 uppercase tracking-wide">{$_('portfolio.total_gain')}</p>
				<p class="text-2xl font-bold tabular-nums truncate mt-0.5
					{totalGain >= 0 ? 'text-green-600 dark:text-green-400' : 'text-red-600 dark:text-red-400'}">
					{totalGain >= 0 ? '+' : ''}{formatCurrency(totalGain)}
				</p>
			</div>
		</div>

		<!-- ETF count -->
		<div class="bg-white dark:bg-slate-800 rounded-xl border border-gray-200 dark:border-slate-700 p-5 shadow-sm flex items-center gap-4">
			<div class="w-11 h-11 rounded-lg bg-slate-100 dark:bg-slate-700 flex items-center justify-center shrink-0">
				<Icon icon="heroicons:squares-2x2" class="w-6 h-6 text-slate-500 dark:text-slate-400" />
			</div>
			<div class="min-w-0">
				<p class="text-xs text-gray-500 dark:text-slate-400 uppercase tracking-wide">{$_('portfolio.etf_count')}</p>
				<p class="text-2xl font-bold tabular-nums mt-0.5">{etfs.length}</p>
			</div>
		</div>
	</div>

	<!-- ── ETF table ──────────────────────────────────────────────────────── -->
	<div class="bg-white dark:bg-slate-800 rounded-xl border border-gray-200 dark:border-slate-700 shadow-sm overflow-hidden">
		<table class="w-full text-sm">
			<thead>
				<tr class="border-b border-gray-100 dark:border-slate-700">
					<th class="text-left px-4 py-3 text-xs font-medium text-gray-500 dark:text-slate-400 uppercase tracking-wide">{$_('portfolio.col_name')}</th>
					<th class="text-left px-4 py-3 text-xs font-medium text-gray-500 dark:text-slate-400 uppercase tracking-wide hidden sm:table-cell">{$_('portfolio.col_wkn')}</th>
					<th class="text-right px-4 py-3 text-xs font-medium text-gray-500 dark:text-slate-400 uppercase tracking-wide">{$_('portfolio.col_value')}</th>
					<th class="text-right px-4 py-3 text-xs font-medium text-gray-500 dark:text-slate-400 uppercase tracking-wide hidden md:table-cell">{$_('portfolio.col_gain')}</th>
					<th class="text-right px-4 py-3 text-xs font-medium text-gray-500 dark:text-slate-400 uppercase tracking-wide hidden lg:table-cell">{$_('portfolio.col_date')}</th>
					<th class="px-4 py-3"></th>
				</tr>
			</thead>
			<tbody class="divide-y divide-gray-100 dark:divide-slate-700">
				{#each etfs as etf (etf.id)}
					<tr class="hover:bg-gray-50 dark:hover:bg-slate-700/50 transition-colors">
						<!-- Name + broker -->
						<td class="px-4 py-3">
							<a href="{base}/portfolio/{etf.id}" class="font-medium hover:text-blue-600 dark:hover:text-blue-400 transition-colors">
								{etf.name}
							</a>
							{#if etf.brokerName}
								<p class="text-xs text-gray-400 dark:text-slate-500">{etf.brokerName}</p>
							{/if}
						</td>

						<!-- WKN -->
						<td class="px-4 py-3 hidden sm:table-cell">
							<span class="font-mono text-xs bg-gray-100 dark:bg-slate-700 px-2 py-0.5 rounded">{etf.wkn}</span>
						</td>

						<!-- Current value -->
						<td class="px-4 py-3 text-right tabular-nums font-semibold">
							{#if etf.latestSnapshot}
								{formatCurrency(etf.latestSnapshot.totalValue)}
							{:else}
								<span class="text-gray-400 dark:text-slate-500">{$_('portfolio.no_snapshot')}</span>
							{/if}
						</td>

						<!-- Gain (absolute + percent) -->
						<td class="px-4 py-3 text-right hidden md:table-cell">
							{#if etf.latestSnapshot?.gainAbsolute != null}
								<span class="tabular-nums font-medium {etf.latestSnapshot.gainAbsolute >= 0 ? 'text-green-600 dark:text-green-400' : 'text-red-600 dark:text-red-400'}">
									{etf.latestSnapshot.gainAbsolute >= 0 ? '+' : ''}{formatCurrency(etf.latestSnapshot.gainAbsolute)}
								</span>
								{#if etf.latestSnapshot.gainPercent != null}
									<p class="text-xs {etf.latestSnapshot.gainPercent >= 0 ? 'text-green-500 dark:text-green-500' : 'text-red-500 dark:text-red-500'}">
										{etf.latestSnapshot.gainPercent >= 0 ? '+' : ''}{etf.latestSnapshot.gainPercent.toFixed(2)} %
									</p>
								{/if}
							{:else}
								<span class="text-gray-400 dark:text-slate-500">{$_('portfolio.no_snapshot')}</span>
							{/if}
						</td>

						<!-- Date -->
						<td class="px-4 py-3 text-right text-xs text-gray-400 dark:text-slate-500 hidden lg:table-cell">
							{#if etf.latestSnapshot}
								{formatDate(etf.latestSnapshot.date)}
							{:else}
								{$_('portfolio.no_snapshot')}
							{/if}
						</td>

						<!-- Actions -->
						<td class="px-4 py-3 text-right">
							{#if confirmDeleteId === etf.id}
								<div class="flex items-center justify-end gap-2">
									{#if deleteError}
										<span class="text-xs text-red-600 dark:text-red-400 max-w-[12rem] text-right">
											{deleteError}
										</span>
									{:else}
										<span class="text-xs text-gray-600 dark:text-slate-300">{$_('portfolio.delete_confirm')}</span>
										<button
											onclick={() => deleteEtf(etf.id)}
											class="text-xs px-2 py-1 rounded-lg bg-red-600 hover:bg-red-700 text-white transition-colors"
										>
											{$_('portfolio.delete_yes')}
										</button>
									{/if}
									<button
										onclick={() => { confirmDeleteId = null; deleteError = ''; }}
										class="text-xs px-2 py-1 rounded-lg border border-gray-300 dark:border-slate-600
										       hover:bg-gray-100 dark:hover:bg-slate-700 transition-colors"
									>
										{$_('common.btn_cancel')}
									</button>
								</div>
							{:else}
								<div class="flex items-center justify-end gap-1">
									<a
										href="{base}/portfolio/{etf.id}"
										class="p-1.5 rounded-lg text-gray-400 dark:text-slate-500
										       hover:bg-gray-100 dark:hover:bg-slate-700 hover:text-gray-700 dark:hover:text-white transition-colors"
										aria-label="Edit"
									>
										<Icon icon="heroicons:pencil-square" class="w-4 h-4" />
									</a>
									<button
										onclick={() => { confirmDeleteId = etf.id; deleteError = ''; }}
										class="p-1.5 rounded-lg text-gray-400 dark:text-slate-500
										       hover:bg-red-50 dark:hover:bg-red-950/30 hover:text-red-600 dark:hover:text-red-400 transition-colors"
										aria-label="Delete"
									>
										<Icon icon="heroicons:trash" class="w-4 h-4" />
									</button>
								</div>
							{/if}
						</td>
					</tr>
				{/each}
			</tbody>
		</table>
	</div>
{/if}
